def call() {
    //deleteDir()

    checkout scm

    //if ("${env.BRANCH_NAME}".startsWith("release")) {
        //def branchMajorMinorVersion = "${env.BRANCH_NAME}".substring("${env.BRANCH_NAME}".lastIndexOf("/") + 1)

        env.BUILD_VERSION = "0.0.${env.BUILD_NUMBER}"//"${nextPatchVersion}"
        currentBuild.displayName = "${env.BUILD_VERSION}"
    //}
}
