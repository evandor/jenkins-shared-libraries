def call() {
    if ("${env.BRANCH_NAME}".startsWith("release")) {

        def thisBranchVersion = "${env.BRANCH_NAME}".substring("${env.BRANCH_NAME}".lastIndexOf("/") + 1)

        // get all branches that start with remotes/origin/release. '|| true' is added to prevent the build from failing if no branches exist
        def releaseBranches = sh script: '(git branch -a | grep remotes/origin/release*) || true', returnStdout: true

        releaseBranches.readLines().each {
            def itBranchVersion = "${it}".substring("${it}".lastIndexOf("/") + 1)
            if (itBranchVersion.toFloat() > thisBranchVersion.toFloat()) {
                withCredentials([usernamePassword(credentialsId: 'xxx',
                        usernameVariable: 'ACCESS_TOKEN_USERNAME',
                        passwordVariable: 'ACCESS_TOKEN_PASSWORD',)]) {
                    lock("${env.PROJECT_NAME}-merge") {
                        try {
                            // We need git reset because sometimes the gradle wrapper file has been changed and
                            // the checkout of master would fail
                            sh "git reset --hard"
                            sh "git checkout release/${itBranchVersion}"
                            sh "git merge origin/${env.BRANCH_NAME}"
                            sh "git push origin release/${itBranchVersion}"
                        } catch (any) {
                            // If there is an error doing the merge we don't want the build to fail.
                            // Try-catch is used because modifying the currentBuild.result to a better state
                            // (e.g. failure -> unstable) isn't possible due to implementation restrictions
                            // in jenkins
                            currentBuild.result = 'UNSTABLE'
                            echo "Merge in branch ${it} failed"
                        }
                    }
                }
            }
        }
    } else {
        echo "Skipped merge, because this is no release branch"
    }
}
