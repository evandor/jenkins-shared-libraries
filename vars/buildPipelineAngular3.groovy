
def publishHTMLReports(reportName) {
    publishHTML(target: [
            allowMissing         : false,
            alwaysLinkToLastBuild: false,
            keepAll              : true,
            reportDir            : 'web-client-e2e-test/build/reports/tests/chromeHeadlessTest',
            reportFiles          : 'index.html',
            reportName           : reportName
    ])
}

def call(project, modulePath, baseUrl) {

    env.PROJECT_NAME = project
    env.MODULE_PATH = modulePath

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        stages {

            stage('Prepare') {
                steps {
                    stagePrepareServices()
                    checkout scm
                }

            }

            stage('Build') {
                steps {
                    //
                    //sh "cd ${env.MODULE_PATH} && npm rebuild node-sass"
                    sh "cd ${env.MODULE_PATH} && ./gradlew -DbuildVersion=${env.BUILD_VERSION} --stacktrace --continue clean build"
                }
            }

            stage('Build Docker Images') {
                steps {
                    sh "cd ${env.MODULE_PATH} && ./gradlew docker --info -DbuildVersion=${env.BUILD_VERSION}"
                }
            }

            stage ('Push 2 Docker.io') {
                steps {
                    sh "docker --version"
                    withCredentials([usernamePassword(credentialsId: 'bf66749d-c1bc-4841-a61c-83bf3f61e166',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD',)]) {
                        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                        sh "docker push $DOCKER_USERNAME/$project:${env.BUILD_VERSION}"
                    }
                }
            }

            /*stage ('Restart Remote Container (test)') {
                steps{
                    // see https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/
                    // https://stackoverflow.com/questions/37603621/jenkins-sudo-no-tty-present-and-no-askpass-program-specified-with-nopasswd
                    sshagent(credentials : ['sailor1']) {
                        sh 'ssh -o StrictHostKeyChecking=no carsten@185.183.96.103 uptime ${project}'
                        sh 'ssh -o StrictHostKeyChecking=no carsten@185.183.96.103 whoami'
                        sh "ssh -o StrictHostKeyChecking=no carsten@185.183.96.103 dockerRun  ${project} test ${env.BUILD_VERSION}"
                    }
                }
            }*/

            stage ('Restart Remote Container (test)') {
                steps{
                    sh "/home/carsten/bin/dockerRun  ${project} test ${env.BUILD_VERSION}"
                }
            }


        }
        post {
            failure {
                emailext body: '$DEFAULT_CONTENT',
                        recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                        ],
                        replyTo: '$DEFAULT_REPLYTO',
                        subject: '$DEFAULT_SUBJECT',
                        to: '$DEFAULT_RECIPIENTS'
            }
        }
    }
}
