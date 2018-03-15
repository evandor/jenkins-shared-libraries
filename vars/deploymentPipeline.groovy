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

    env.INSTANCE_NAME
    env.APPLICATION_KEY
    env.APPLICATION_VERSION

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
        }

        stages {
            stage('Select Instance') {
                steps {
                    stageSelectApplication()
                }
            }

           /*stage('Select Application') {
                steps {
                    stageSelectApplication(env.INSTANCE_NAME)
                }
            }*/

            stage('Select Version') {
                steps {
                    stageSelectVersion(env.INSTANCE_NAME)
                }
            }

            /*stage('Copy to Deployment Repository') {
                when {
                    expression { deployEnvironment == Environment.ABN }
                }
                steps {
                    stageCopyToDeploymentRepo(env.APPLICATION_KEY, env.APPLICATION_VERSION)
                }
            }*/

            stage('Deploy') {
                steps {
                    script {
                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            //sh "./skysail.server.website/release/deployment/scripts/run_docker_test.sh &"
                            //sh "/home/carsten/skysail/skysailconfigs/website/test/deploy/run_docker.sh"
                            sh "/home/carsten/install/docker/skysail/run_docker.sh website test ${env.BUILD_VERSION} &"
                            sh "/home/carsten/install/docker/skysail/run_docker.sh demo test ${env.BUILD_VERSION} &"
                        }
                    }
                }
            }
        }
    }
}
