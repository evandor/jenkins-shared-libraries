def call() {
    sh "chmod +x gradlew"
    sh "./gradlew -Pversion=${env.BUILD_VERSION} --stacktrace --continue -Pa=b -Dc=INT clean build"
}
