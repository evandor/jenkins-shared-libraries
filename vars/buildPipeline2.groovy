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
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue clean build -x test -x check"
                }
            }

            stage('Test & Coverage') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue test"
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} reportScoverage"
                    //step([$class: 'ScoveragePublisher', reportDir: 'skysail.api/generated/reports/scoverage', reportFile: 'scoverage.xml'])
                    step([$class: 'ScoveragePublisher', reportDir: 'skysail.domain/generated/reports/scoverage', reportFile: 'scoverage.xml'])

                    withSonarQubeEnv('sonar') {
                        // requires SonarQube Scanner for Gradle 2.1+
                        // It's important to add --info because of SONARJNKNS-281
                        sh './gradlew --info sonarqube'
                    }

                }

                post {
                    always {
                        junit "**/test-reports/test/TEST-*.xml"
                    }

                }
            }

            stage('Check') {
                steps {
                    sh "./gradlew -Pversion=${env.BUILD_VERSION} -DbuildVersion=jenkins-${env.BUILD_VERSION} --stacktrace --continue check"
                }
            }

            stage('Tag') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'd04cfe1a-4efc-4a0a-b65b-4775a1a15a14',
                            usernameVariable: 'ACCESS_TOKEN_USERNAME',
                            passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                        sh "git remote set-url origin https://$ACCESS_TOKEN_USERNAME:$ACCESS_TOKEN_PASSWORD@github.com/evandor/skysail-server"
                        //sh "git push origin :refs/tags/${env.BUILD_VERSION}"
                        sh "git push --force origin refs/tags/${env.BUILD_VERSION}:refs/tags/${env.BUILD_VERSION}"
                        sh "git tag -d ${env.BUILD_VERSION}"
                        sh "git tag -m '' ${env.BUILD_VERSION}"
                        sh "git pull --tags"
                        sh "git push --tags"
                    }
                }
            }

            stage('Export Jars') {
                steps {
                   // sh './gradlew skysail.server:export.server.docker'
                    sh './gradlew skysail.server.demo:export.demo'
                    sh './gradlew skysail.server.website:export.website'
                }
            }

            stage ('Build Docker Images') {
                steps {
                    //sh './gradlew skysail.server:runnable skysail.server:buildImage'
                    sh "./gradlew skysail.server.demo:runnable skysail.server.demo:buildImage -Pversion=${env.BUILD_VERSION}"
                    sh "./gradlew skysail.server.website:runnable skysail.server.website:buildImage -Pversion=${env.BUILD_VERSION}"
                }
            }

            stage ('Restart Containers') {
                steps {
                    script {
                        sh "svn update /home/carsten/skysail/skysailconfigs/"
                        sh "svn update /home/carsten/install/docker/"

                        //withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                        //    sh "./skysail.server/release/deployment/scripts/run_docker.sh &"
                        //}
                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            //sh "./skysail.server.website/release/deployment/scripts/run_docker_test.sh &"
                            //sh "/home/carsten/skysail/skysailconfigs/website/test/deploy/run_docker.sh"
                            sh "/home/carsten/install/docker/skysail/run_docker.sh website test ${env.BUILD_VERSION} &"
                            sh "/home/carsten/install/docker/skysail/run_docker.sh demo test ${env.BUILD_VERSION} &"
                        }
                    }
                }
            }

            stage ('Push 2 Docker.io') {
                steps {
                    sh "docker --version"
                    sh "docker images"
                    withCredentials([usernamePassword(credentialsId: 'bf66749d-c1bc-4841-a61c-83bf3f61e166',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD',)]) {
                        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                        sh "docker push $DOCKER_USERNAME/skysail-server-demo:${env.BUILD_VERSION}"
                    }
                }
            }

            stage ('Gatling') {
                steps {
                    sh './gradlew skysail.server.demo.guitests:gatlingRun'
                    gatlingArchive()
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
                emailext body: '$DEFAULT_CONTENT',
                        recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                        ],
                        replyTo: '$DEFAULT_REPLYTO',
                        subject: '$DEFAULT_SUBJECT',
                        to: '$DEFAULT_RECIPIENTS'
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
