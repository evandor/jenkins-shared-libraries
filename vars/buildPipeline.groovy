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
                    //stagePrepareServices()
                    checkout scm
                }

            }

            stage('Build') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue clean build"
                    /*withCredentials([usernamePassword(credentialsId: 'd04cfe1a-4efc-4a0a-b65b-4775a1a15a14',
                            usernameVariable: 'ACCESS_TOKEN_USERNAME',
                            passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                        sh "git remote set-url origin https://$ACCESS_TOKEN_USERNAME:$ACCESS_TOKEN_PASSWORD@github.com/evandor/services"
                        sh "git tag -m '' ${env.BUILD_VERSION}"
                        sh "git pull --tags"
                        sh "git push --tags"
                    }*/
                }
                /*post {
                    always {
                        junit "** /test-reports/test/TEST-*.xml"
                    }
                }*/
            }

            stage('Coverage') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue clean build"
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} reportScoverage"
                }
            }


            stage('sonar') {
                steps {
                    sh "./gradlew sonar --info"
                }
            }

            stage ('Build Docker Images') {
                steps {
                    sh "./gradlew docker --info"
                }
            }

            stage ('Restart Containers') {
                steps {
                    script {
                        sh "cd /home/carsten/install/docker/"
                        sh "git pull --rebase"

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "/home/carsten/install/docker/services/run_docker.sh skysail-service-monitor test ${env.BUILD_VERSION} &"
                        }
                    }
                }
            }


            stage ('Document') {
                steps {
                    //sh "./gradlew asciidoctor"
                    sh "./gradlew scaladoc"
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
