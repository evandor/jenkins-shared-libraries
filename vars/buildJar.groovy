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
                        junit "** /test-results/test/TEST-*.xml"
                    }
                }*/
            }

            stage('Document') {
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
