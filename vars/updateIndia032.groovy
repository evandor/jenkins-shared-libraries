def call() {

    user = "carsten"
    target = "carsten.evandor.de"

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
                    sh "cp -r india032/crontab/* /home/carsten/bin/"
                    sh "chmod 775 /home/carsten/bin/*.sh"
                }
            }

            stage ('Copy apache files...') {
                steps{
                    sshagent(credentials : ['fecd2933-c9aa-4998-9806-8f13bb88bbbc']) {
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} uptime"
                        sh "scp -o StrictHostKeyChecking=no -r india032/sites-available/ ${user}@${target}:/etc/apache2"
                        sh "scp -o StrictHostKeyChecking=no ${user}@${target} cd /etc/apache2/sites-available && sudo apachectl configtest"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo service apache2 reload"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo systemctl status apache2.service"
                        sh "ssh -o StrictHostKeyChecking=no ${user}@${target} sudo journalctl -xn"
                    }

                }
            }

        }
    }
}
