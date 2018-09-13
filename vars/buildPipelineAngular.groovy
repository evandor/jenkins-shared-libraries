/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 */

def call(project, modulePath, runGatling) {

    env.PROJECT_NAME = project
    env.MODULE_PATH = modulePath
    env.RUN_GATLING = runGatling

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
                    sh "./gradlew -DbuildVersion=${env.BUILD_VERSION} --stacktrace --continue clean build"
                }
                /*post {
                    always {
                        //junit "** /test-reports/test/TEST-*.xml"
                    }
                }*/
            }

            stage('Build Docker Images') {
                steps {
                    sh "./gradlew docker --info -DbuildVersion=${env.BUILD_VERSION}"
                }
            }

            stage('Restart Containers') {
                steps {
                    script {
                        //sh "cd /home/carsten/install/docker/"
                        //sh "git pull --rebase"

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "/home/carsten/install/docker/services/run_docker.sh ${project} tst ${env.BUILD_VERSION}"
                        }
                        sh "docker --version"
                        sh "docker images"

                    }
                }
            }

            /*stage('Gatling') {
                when {
                    expression {
                        return env.RUN_GATLING == "true"; //???
                    }
                }
                steps {
                    sh './gradlew monitor-server-loadtest:gatlingRun'
                    gatlingArchive()
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
