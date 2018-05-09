def call(app, env, version) {
    sh "nohup /home/carsten/install/docker/services/run_docker_with_hostnet.sh " + app + " " + env + " " + version
}
