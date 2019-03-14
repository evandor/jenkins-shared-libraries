def call() {

    // jenkins job: http://jenkins.skysail.io/job/infrastructure-docker/
    // deploys dockerRun on all machines

    user1 = "carsten"
    target1 = "185.141.27.91"

    user2 = "ec2-user"
    target2 = "ec2-34-246-151-3.eu-west-1.compute.amazonaws.com"

    user3 = "ec2-user"
    target3 = "ec2-63-34-158-91.eu-west-1.compute.amazonaws.com"

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
                        sh "ssh -o StrictHostKeyChecking=no ${user1}@${target1} mkdir -p /home/${user1}/bin"
                        sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user1}@${target1}:/home/${user1}/bin/dockerRun"

                        // target2
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} uptime"
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} whoami"
                        sh "ssh -o StrictHostKeyChecking=no ${user2}@${target2} mkdir -p /home/${user2}/bin"
                        sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user2}@${target2}:/home/${user2}/bin/dockerRun"

                        // target3
                        //sh "ssh -o StrictHostKeyChecking=no ${user3}@${target3} uptime"
                        //sh "ssh -o StrictHostKeyChecking=no ${user3}@${target3} whoami"
                        //sh "ssh -o StrictHostKeyChecking=no ${user3}@${target3} mkdir -p /home/${user3}/bin"
                        //sh "scp -o StrictHostKeyChecking=no docker/dockerRun ${user3}@${target3}:/home/${user3}/bin/dockerRun"

                        // local machine
                        sh "cp docker/dockerRun /home/carsten/bin/dockerRun"

                    }
                }
            }

        }
    }
}
