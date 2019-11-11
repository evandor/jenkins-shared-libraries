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
    //user = "ec2-user"
    //target = "ec2-34-246-151-3.eu-west-1.compute.amazonaws.com"
    user = "carsten"
    target = "185.141.27.91"

    //user = "ec2-user"
    //target = "ec2-63-34-158-91.eu-west-1.compute.amazonaws.com"

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
                    /*withCredentials([usernamePassword(credentialsId: 'd04cfe1a-4efc-4a0a-b65b-4775a1a15a14',
                            usernameVariable: 'ACCESS_TOKEN_USERNAME',
                            passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                        sh "git remote set-url origin https://$ACCESS_TOKEN_USERNAME:$ACCESS_TOKEN_PASSWORD@github.com/evandor/services"
                        sh "git tag -m '' ${env.BUILD_VERSION}"
                        sh "git pull --tags"
                        sh "git push --tags"
                    }*/
                }
                /*post {
                    always {
                        junit "** /test-reports/test/TEST-*.xml"
                    }
                }*/
            }

            stage('Build Docker Images') {
                steps {
                    sh "./gradlew docker --info -DbuildVersion=${env.BUILD_VERSION}"
                    sh "./gradlew docker --info -DbuildVersion=latest"
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
                        sh "docker push $DOCKER_USERNAME/$project:latest"
                        //sh "docker push $DOCKER_USERNAME/$project:latest"
                    }
                }
            }

           /* stage('Start container on AWS') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} /home/${user}/bin/dockerRun ${project} prod ${env.BUILD_VERSION}"

                        //sh "scp -o StrictHostKeyChecking=no -i '/root/.ssh/skysail.pem' -r aws/ec2-34-246-151-3/apache/conf.d ${user}@${target}:/home/ec2-user/jenkinstarget/apache/"
                        //sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service httpd restart"
                    }
                }
            }*/

            stage('Start container on HostSailor') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} /home/${user}/bin/dockerRun ${project} prod ${env.BUILD_VERSION}"

                        //sh "scp -o StrictHostKeyChecking=no -i '/root/.ssh/skysail.pem' -r aws/ec2-34-246-151-3/apache/conf.d ${user}@${target}:/home/ec2-user/jenkinstarget/apache/"
                        //sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service httpd restart"
                    }
                }
            }


            stage('Document') {
                steps {
                    //sh "./gradlew asciidoctor"
                    sh "./gradlew javadoc"
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
