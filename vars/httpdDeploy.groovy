def call(user, target) {

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        stages {

            stage('Prepare') {
                steps {
                    //stagePrepareServices()
                    checkout scm
                }

            }

            stage ('Copy dockerRun to target...') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@ec2-34-246-151-3.eu-west-1.compute.amazonaws.com uptime ${project}"
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@ec2-34-246-151-3.eu-west-1.compute.amazonaws.com uptime whoami"
                        //sh 'scp -o StrictHostKeyChecking=no docker/dockerRun carsten@185.183.96.103:/home/carsten/bin/dockerRun'
                    }
                }
            }

        }
    }
}
