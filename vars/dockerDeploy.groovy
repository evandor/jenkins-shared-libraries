def call(user1, target1, user2, target2) {

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

            stage ('Copy dockerRun to targets') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} whoami"
                        sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user1}@${target1}:/home/${user1}/bin/dockerRun"

                        //sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} uptime"
                        //sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} whoami"
                        //sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user2}@${target2}:/home/${user2}/bin/dockerRun"
                        sh "cp docker/dockerRun /home/carsten/bin/dockerRun"

                    }
                }
            }

        }
    }
}
