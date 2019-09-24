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

            stage('Copy crontab files...') {
                steps {
                    sh "scp -i /root/.ssh/skysail.pem -r aws/ec2-34-246-151-3/crontab/* ${user}@${target}:/home/ec2-user/bin/"
                    sh "ssh -i /root/.ssh/skysail.pem ${user}@${target} chmod 775 /home/ec2-user/bin/*.sh"
                }
            }

            stage ('Copy apache files...') {
                steps{
                    sshagent(credentials : ['fecd2933-c9aa-4998-9806-8f13bb88bbbc']) {
                        //sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        //sh "scp -o StrictHostKeyChecking=no -r india032/sites-available/ ${user}@${target}:/etc/apache2"
                        //sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service apache2 reload"
                    }

                }
            }

            stage ('Copy dockerRun to target...') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        sh "scp -o StrictHostKeyChecking=no -i '/root/.ssh/skysail.pem' -r aws/ec2-34-246-151-3/apache/conf.d ${user}@${target}:/home/ec2-user/jenkinstarget/apache/"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service httpd restart"
                    }
                }
            }


        }
    }
}
