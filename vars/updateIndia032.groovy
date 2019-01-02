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

            stage('Copy files...') {
                steps {
                    sh "cp crontab/cert-renew.sh /home/carsten/bin/cert-renew.sh"
                }
            }

        }
    }
}
