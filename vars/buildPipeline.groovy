/**
 * Pipeline Definition File
 * Information about the syntax can be found here: https://jenkins.io/doc/book/pipeline/syntax/
 *
 * To run this File as a Pipeline job atleast the following plugins are required:
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Model+Definition+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Utility+Steps+Plugin
 * https://wiki.jenkins-ci.org/display/JENKINS/Lockable+Resources+Plugin
 */

def call(project, modulePath) {

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
                post {
                    always {
                        //junit "**/test-reports/test/TEST-*.xml"
                        junit "**/test-results/test/TEST-*.xml"
                    }
                }
            }

            stage('Coverage') {
                steps {
                    //sh "./gradlew --stacktrace --continue clean build"
                    sh "./gradlew reportScoverage"
                    step([$class: 'ScoveragePublisher', reportDir: env.MODULE_PATH + 'build/reports/scoverage', reportFile: 'scoverage.xml'])

                    /*withSonarQubeEnv('sonar') {
                        // requires SonarQube Scanner for Gradle 2.1+
                        // It's important to add --info because of SONARJNKNS-281
                        sh './gradlew --info sonarqube'
                    }*/

                }
            }

            /*stage('sonar') {
                steps {
                    sh "./gradlew sonar"
                }
            }*/

            stage('Build Docker Images') {
                steps {
                    sh "./gradlew docker --info -DbuildVersion=${env.BUILD_VERSION}"
                }
            }

            stage('Restart Containers') {
                steps {
                    script {
                        //sh "cd /home/carsten/install/docker/"
                        //sh "git pull --rebase"

                        withEnv(['JENKINS_NODE_COOKIE =dontkill']) {
                            sh "/home/carsten/install/docker/services/run_docker.sh ${project} tst ${env.BUILD_VERSION}"
                        }
                        sh "docker --version"
                        sh "docker images"

                    }
                }
            }

            stage('Gatling') {
                steps {
                    sh './gradlew server-loadtest:gatlingRun'
                    gatlingArchive()
                }
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
