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


def call(project, modulePath, theStage) {

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
                    sh "cd ${env.MODULE_PATH} && ./gradlew -DbuildVersion=${env.BUILD_VERSION} --stacktrace --continue clean build"
                }
                post {
                    always {
                        junit "**/test-results/test/TEST-*.xml"
                    }
                }
            }

            stage('Build Docker Images') {
                steps {
                    sh "cd ${env.MODULE_PATH} && ./gradlew docker --info -DbuildVersion=${env.BUILD_VERSION}"
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
                        sh "docker push $DOCKER_USERNAME/$project:${env.BUILD_VERSION}"
                    }
                }
            }

            stage('Restart Containers') {
                steps {
                    script {

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "/home/carsten/install/docker/services/run_docker.sh ${project} ${theStage} ${env.BUILD_VERSION}"
                        }
                        sh "docker --version"
                        sh "docker images"

                    }
                }
            }

            stage('Document') {
                steps {
                    //sh "./gradlew asciidoctor"
                    sh "cd ${env.MODULE_PATH} && ./gradlew scaladoc"
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
