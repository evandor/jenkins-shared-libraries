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
    env.MODULE_PATH = "."

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
            sidebarLinks([
                   // [displayName: 'sonar', iconFileName: 'help', urlName: 'http://85.25.22.126:9000'],
                    [displayName: 'logs', iconFileName: 'help', urlName: 'https://app.logz.io/#/dashboard/kibana'],
                    [displayName: 'actuator', iconFileName: 'help', urlName: 'http://185.141.27.91:6011'],
                    [displayName: 'scaladoc', iconFileName: 'help', urlName: 'http://185.141.27.91:6011'],
                    [displayName: 'oneSignal', iconFileName: 'help', urlName: 'https://app.onesignal.com/apps/26dd1c4f-1f9c-4a51-b862-e080d8aedc64'],
                    [displayName: 'monicat-backend.skysail.io', iconFileName: 'help', urlName: 'http://monicat-backend.skysail.io/']
            ])
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
                    sh "cd ${env.MODULE_PATH} && ./gradlew -Dconfig.resource=ci.conf docker --info -DbuildVersion=${env.BUILD_VERSION}"
                }
            }

            stage('Push 2 Docker.io') {
                steps {
                    sh "docker --version"
                    //sh "docker images"
                    withCredentials([usernamePassword(credentialsId: 'bf66749d-c1bc-4841-a61c-83bf3f61e166',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD',)]) {
                        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                        sh "docker push $DOCKER_USERNAME/$project"
                        sh "docker push $DOCKER_USERNAME/$project:${env.BUILD_VERSION}"
                        //sh "docker push $DOCKER_USERNAME/$project:latest"
                    }
                }
            }

            stage("Restart Container") {
                steps {
                    sh "/home/carsten/bin/dockerRun ${project} ${theStage} ${env.BUILD_VERSION}"
                }
            }

            /*stage ('Deployment Prod') {
                steps{
                    stageProceedToProd(env.CONTINUE_TO_PROD)

                    sh "/home/carsten/bin/dockerRun ${project} prod ${env.BUILD_VERSION}"
                }
            }*/

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

            /*stage('Sonar') {
                steps {
                    sh "cd ${env.MODULE_PATH} && ./gradlew -Dconfig.resource=ci.conf reportScoverage --info -DbuildVersion=${env.BUILD_VERSION}"
                    sh "cd ${env.MODULE_PATH} && ./gradlew -Dconfig.resource=ci.conf sonarqube --info -DbuildVersion=${env.BUILD_VERSION}"
                }
                post {
                    always {
                        publishHTML([reportTitles         : 'Sonar Test Results',
                                     allowMissing         : false,
                                     alwaysLinkToLastBuild: true,
                                     keepAll              : true,
                                     reportDir            : 'build/reports/scoverage',
                                     reportFiles          : 'index.html,overview.html,packages.html',
                                     reportName           : 'CoverageReport'
                        ])
                    }
                }
            }*/

            /*stage('Document') {
                steps {
                    //sh "cd ${env.MODULE_PATH} && ./gradlew -Dconfig.resource=ci.conf scaladoc asciidoc -DbuildVersion=${env.BUILD_VERSION}"
                }
            }*/
            /*stage('Publish Asciidoc') {
                steps {
                    publishHTML([reportTitles         : 'Documentation',
                                 allowMissing         : false,
                                 alwaysLinkToLastBuild: true,
                                 keepAll              : true,
                                 reportDir            : 'build/docs/html5',
                                 reportFiles          : 'monicat-backend.html,api-guide.html',
                                 reportName           : 'Docs'
                    ])

                }
            }*/
            /*stage('Publish Scaladoc') {
                steps {
                    publishHTML([reportTitles         : 'Technical Documentation',
                                 allowMissing         : false,
                                 alwaysLinkToLastBuild: true,
                                 keepAll              : true,
                                 reportDir            : 'build/docs/scaladoc',
                                 reportFiles          : 'index.html',
                                 reportName           : 'Scaladoc'
                    ])

                }
            }*/


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
