/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 */

def call(project) {

    env.PROJECT_NAME = project

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        stages {

            stage('Prepare') {
                steps {
                    stagePrepareApps()
                }

            }

            stage('Build') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue clean build"
//                    lock("${env.PROJECT_NAME}-db") {
//                        stageUnitTests()
//                    }
                    withCredentials([usernamePassword(credentialsId: 'd04cfe1a-4efc-4a0a-b65b-4775a1a15a14',
                            usernameVariable: 'ACCESS_TOKEN_USERNAME',
                            passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                        sh "git remote set-url origin https://$ACCESS_TOKEN_USERNAME:$ACCESS_TOKEN_PASSWORD@github.com/evandor/skysail-apps"
                        //sh "git push --force origin refs/tags/${env.BUILD_VERSION}:refs/tags/${env.BUILD_VERSION}"
                        //sh "git tag -d ${env.BUILD_VERSION}"
                        sh "git tag -m '' ${env.BUILD_VERSION}"
                        sh "git pull --tags"
                        sh "git push --tags"
                    }
                }
                post {
                    always {
                        junit "**/test-reports/test/TEST-*.xml"
                    }
                }
            }

           /* stage('Export Jars') {
                steps {
                    sh './gradlew skysail.server:export.server.test'
                    sh './gradlew skysail.server:export.server.int'
                    sh './gradlew skysail.server.website:export.server.website'
                }
            }

            stage ('Build Docker Images') {
                steps {
                    sh 'sudo ./gradlew skysail.server:runnable skysail.server:buildImage'
                    sh 'sudo ./gradlew skysail.server.website:runnable skysail.server.website:buildImage'
                }
            }

            stage ('Restart Containers') {
                steps {
                    script {
                        sh "svn update /home/carsten/skysail/skysailconfigs/"
                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "sudo ./skysail.server/release/deployment/scripts/run_docker.sh &"
                        }
                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "sudo ./skysail.server.website/release/deployment/scripts/run_docker_test.sh &"
                        }
                    }
                }
            }*/

            stage ('Document') {
                steps {
                    //sh "./gradlew asciidoctor"
                    sh "./gradlew scaladoc"
                }
            }


            /*stage('Sonar') {
                steps {
                    stageSonar()
                }
            }

            stage('Deploy') {
                steps {
                    stageDeploy()
                }
            }*/

            /*stage('merge in master') {
                steps {
                    stageMergeMaster()
                }
            }*/

            /*stage('merge in higher versions') {
                steps {
                    stageMergeHigherVersion()
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
