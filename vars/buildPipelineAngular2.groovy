/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 */

def publishHTMLReports(reportName) {
    publishHTML (target: [
            allowMissing: false,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'web-client-e2e-test/build/reports/tests/chromeHeadlessTest',
            reportFiles: 'index.html',
            reportName: $reportName
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

            stage('Restart Containers') {
                steps {
                    script {

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "/home/carsten/install/docker/services/run_docker.sh ${project} test ${env.BUILD_VERSION}"
                        }
                        sh "docker --version"
                        sh "docker images"

                    }
                }
            }

            stage('GUI Tests') {
                steps {
                    try {
                      script {
                        // -Dgeb.build.baseUrl="http://localhost:4200/" chromeTest
                        sh "cd web-client-e2e-test && ./gradlew -Dgeb.build.baseUrl='$baseUrl' chromeHeadlessTest"
                      }
                    } finally {
                        publishHTMLReports('GUI Tests')
                    }


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
