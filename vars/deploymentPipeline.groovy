import io.skysail.jenkins.config.Environment

/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 */

def call(String deployEnvironment) {

    env.APPLICATION_NAME
    env.APPLICATION_VERSION

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Select Application') {
                steps {
                    stageSelectApplication()
                }
            }

            stage('Select Version') {
                steps {
                    stageSelectVersion(env.APPLICATION_NAME)
                    currentBuild.displayName = "${env.APPLICATION_NAME} ${env.APPLICATION_VERSION}"
                }
            }

            stage('Deploy') {
                steps {
                    timeout(1) {
                        script {
                            withEnv(['JENKINS_NODE_COOKIE=dontkillDeployment']) {
                                stageDeploy(env.APPLICATION_NAME, deployEnvironment, env.APPLICATION_VERSION)
                                //sh "nohup /home/carsten/install/docker/skysail/run_docker.sh website test 0.0.135"
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
