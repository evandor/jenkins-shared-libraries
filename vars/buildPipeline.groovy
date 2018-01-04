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
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {

            stage('Prepare') {
                steps {
                    stagePrepare()
                }

            }

            stage('Build') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue clean build"
//                    lock("${env.PROJECT_NAME}-db") {
//                        stageUnitTests()
//                    }
                    withCredentials([usernamePassword(credentialsId: '52dc175f-8512-45d2-97e6-ebec0e60b907',
                            usernameVariable: 'ACCESS_TOKEN_USERNAME',
                            passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                        sh "git tag -m '' ${env.BUILD_VERSION}"
                        sh "git remote set-url origin https://$ACCESS_TOKEN_USERNAME:$ACCESS_TOKEN_PASSWORD@github.com/evandor/skysail-server"
                        sh "git push --tags"
                    }
                }
                post {
                    always {
                        junit "**/test-reports/test/TEST-*.xml"
                    }
                }
            }

            stage('Export Jars') {
                steps {
                    sh './gradlew skysail.server:export.server.test'
                    sh './gradlew skysail.server:export.server.int'
                    sh './gradlew skysail.server.website:export.server.website'
                    //sh './gradlew skysail.server.demo:export.server.demo.docker'
                }
            }

            stage ('Build Docker Images') {
                steps {
                    sh 'sudo ./gradlew skysail.server:runnable skysail.server:buildImage'
                    sh 'sudo ./gradlew skysail.server.website:runnable skysail.server.website:buildImage'
                    //sh 'sudo ./gradlew skysail.server.demo:runnable skysail.server.demo:buildImage'
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
            }

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
                echo "failure when building..."
                // This script is used to kill pending processes of this job build, because the ProcessTreeKiller won't kill those processes
                // when the e2e tests on start up.
//                sh """#!/bin/sh -e
//                    # get all processes for the given build tag, filter self process and iterate over result
//                    grep -lis 'BUILD_TAG=${env.BUILD_TAG}' /proc/*/environ | grep -v /proc/self/environ | while read -r line; do
//
//                    # extract process id
//                    PID=`echo "\$line" | cut -d\'/\' -f 3`
//
//                    # is process currently running
//                    if [[ -e /proc/\$PID ]]; then
//                      echo "Killing Process with id \$PID"
//                      kill -9 \$PID
//                    fi
//                    done
//                """

            }
        }
    }
}
