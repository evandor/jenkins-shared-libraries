/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 *
 * This is used from the jenkins docker based installation @ http://85.25.22.126:8080
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
                    withCredentials([usernamePassword(credentialsId: 'd04cfe1a-4efc-4a0a-b65b-4775a1a15a14',
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
                    sh './gradlew skysail.server:export.server.docker'
                    sh './gradlew skysail.server.demo:export.server.demo.docker'
                    sh './gradlew skysail.server.website:export.server.website'
                }
            }

            stage ('Build Docker Images') {
                steps {
                    sh './gradlew skysail.server:runnable skysail.server:buildImage'
                    sh './gradlew skysail.server.demo:runnable skysail.server.demo:buildImage'
                    sh './gradlew skysail.server.website:runnable skysail.server.website:buildImage'
                }
            }

            stage ('Restart Containers') {
                steps {
                    script {
                        sh "svn update /home/carsten/skysail/skysailconfigs/"
                        sh "svn update /home/carsten/install/docker/"

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "./skysail.server/release/deployment/scripts/run_docker.sh &"
                        }
                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            //sh "./skysail.server.website/release/deployment/scripts/run_docker_test.sh &"
                            sh "/home/carsten/skysail/skysailconfigs/website/test/deploy/run_docker.sh"
                            sh "/home/carsten/install/docker/skysail/run_docker.sh demo test latest"
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
