/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 */

/*def call(project, modulePath) {
    call(project, modulePath, true)
}*/


def call(project, modulePath, theStage) {

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
                    sh "cd ${env.MODULE_PATH} && ./gradlew -Dconfig.resource=ci.conf -DbuildVersion=${env.BUILD_VERSION} --stacktrace --continue clean build"
                }
                post {
                    always {
                        junit "**/test-results/test/TEST-*.xml"
                    }
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
                    //sh "docker images"
                    withCredentials([usernamePassword(credentialsId: 'bf66749d-c1bc-4841-a61c-83bf3f61e166',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD',)]) {
                        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                        sh "docker push $DOCKER_USERNAME/$project:${env.BUILD_VERSION}"
                        //sh "docker push $DOCKER_USERNAME/$project:latest"
                    }
                }
            }

            stage ('Restart Local Container (test)') {
                steps{
                    sh "/home/carsten/bin/dockerRun ${project} ${theStage} ${env.BUILD_VERSION}"
                }
            }

            /*stage ('Restart Remote Container (test)') {
                steps{
                    // see https://help.github.com/articles/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent/
                    // https://stackoverflow.com/questions/37603621/jenkins-sudo-no-tty-present-and-no-askpass-program-specified-with-nopasswd
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} whoami"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} dockerRun ${project} ${theStage} ${env.BUILD_VERSION}"
                    }
                }
            }*/

            stage('Document') {
                steps {
                    //sh "./gradlew asciidoctor"
                    sh "cd ${env.MODULE_PATH} && ./gradlew scaladoc asciidoc"
                }
            }
            stage('Publish Asciidoc') {
                steps {
                    publishHTML([reportTitles: 'Documentation',
                                 allowMissing: false,
                                 alwaysLinkToLastBuild: true,
                                 keepAll: true,
                                 reportDir: 'monicat-backend/build/docs/html5',
                                 reportFiles: 'monicat-backend.html,api-guide.html',
                                 reportName: 'Docs'
                    ])

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
