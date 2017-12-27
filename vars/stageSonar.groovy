def call() {
    sh "./gradlew --stacktrace --continue sonarqube -x test"
}
