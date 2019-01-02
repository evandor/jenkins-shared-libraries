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

            stage('Copy files...') {
                steps {
                    sh "cp crontab/cert-renew.sh /home/carsten/bin/cert-renew.sh"
                }
            }

        }
    }
}
