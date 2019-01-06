def call() {

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
                    sh "cp india032/crontab/cert-renew.sh /home/carsten/bin/cert-renew.sh"
                    sh "chmod 775 /home/carsten/bin/cert-renew.sh"
                }
            }

            stage ('Copy apache files...') {
                steps{
                    sh "cp -r india032/sites-available/ /etc/apache2"
                    sh "sudo service apache2 reload"
                }
            }

        }
    }
}
