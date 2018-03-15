import io.skysail.jenkins.config.Environment

/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 */

def call(Environment deployEnvironment) {

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
                    stageSelectInstance()
                }
            }

           /* stage('Select Application') {
                steps {
                    stageSelectApplication(env.INSTANCE_NAME)
                }
            }

            stage('Select Version') {
                steps {
                    script {
                        String repositoryId = NexusRepository.getByEnvironment(deployEnvironment).id
                        stageSelectVersion(env.INSTANCE_NAME, env.APPLICATION_KEY, repositoryId)
                    }
                }
            }

            stage('Copy to Deployment Repository') {
                when {
                    expression { deployEnvironment == Environment.ABN }
                }
                steps {
                    stageCopyToDeploymentRepo(env.APPLICATION_KEY, env.APPLICATION_VERSION)
                }
            }

            stage('Deploy') {
                steps {
                    script {
                    }
                }
            }*/
        }
    }
}
