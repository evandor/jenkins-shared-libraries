import io.skysail.jenkins.config.Environment

/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 */

def call() {

    env.APPLICATION_NAME
    env.APPLICATION_VERSION

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Select Environment') {
                steps {
                    stageSelectEnvironment()
                }
            }

            stage('Select Application') {
                steps {
                    stageSelectApplication()
                }
            }

            stage('Select Version!') {
                steps {
                    currentBuild.displayName = "${env.APPLICATION_NAME} xxx"
                    stageSelectVersion(env.APPLICATION_NAME)
                    currentBuild.displayName = "${env.APPLICATION_NAME} ${env.APPLICATION_VERSION}"
                }
                script {
                    currentBuild.displayName = "${env.APPLICATION_NAME} ${env.APPLICATION_VERSION}"
                }
            }

            stage('Deploy') {
                steps {
                    timeout(1) {
                        script {
                            withEnv(['JENKINS_NODE_COOKIE=dontkillDeployment']) {
                                stageDeploy(env.APPLICATION_NAME, env.STAGE, env.APPLICATION_VERSION)
                            }
                        }
                    }
                }
            }

            stage('waiting...') {
                steps {
                    script {
                        sh "sleep 60"
                    }
                }
            }
        }
    }
}
