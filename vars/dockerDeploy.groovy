def call(user1, target1, user2, target2) {

    // jenkins job: http://jenkins.skysail.io/job/infrastructure-docker/
    // deploys dockerRun on all machines

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
                        // target1
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} whoami"
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} mkdir /home/${user1}/bin"
                        sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user1}@${target1}:/home/${user1}/bin/dockerRun"

                        // target2
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} whoami"
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} mkdir /home/${user2}/bin"
                        sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user2}@${target2}:/home/${user2}/bin/dockerRun"

                        // local machine
                        sh "cp docker/dockerRun /home/carsten/bin/dockerRun"

                    }
                }
            }

        }
    }
}
