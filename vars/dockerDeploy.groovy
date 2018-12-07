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

            stage ('Copy dockerRun to 185.183.96.103') {
                steps{
                    sshagent(credentials : ['sailor1']) {
                        sh 'ssh -o StrictHostKeyChecking=no carsten@185.183.96.103 uptime ${project}'
                        sh 'ssh -o StrictHostKeyChecking=no carsten@185.183.96.103 whoami'
                    }
                }
            }

        }
    }
}
