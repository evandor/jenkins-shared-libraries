def call(app, env, version) {
    sh "nohup /home/carsten/install/docker/service/run_docker.sh " + app + " " + env + " " + version
}
