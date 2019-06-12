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

            stage ('Copying sites-available and restarting apache...') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        //sh "scp -o StrictHostKeyChecking=no -i '/root/.ssh/skysail.pem' -r aws/ec2-34-246-151-3/apache/conf.d ${user}@${target}:/home/ec2-user/jenkinstarget/apache/"
                        sh "scp -o StrictHostKeyChecking=no -r sailor1/sites-available/ ${user}@${target}:/etc/apache2"
                        //sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service httpd restart"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo systemctl reload apache2"
                    }
                }
            }

            stage ('Copying docker files to target...') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        // see project http://85.25.22.126:3000/carsten/skysail-infrastructure.git
                        sh "scp -o StrictHostKeyChecking=no -r sailor1/docker/ ${user}@${target}:/home/carsten/"
                    }
                }
            }

        }
    }
}
