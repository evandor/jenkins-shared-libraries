def call(app, env, version) {
    sh "nohup /home/carsten/install/docker/skysail/run_docker.sh " + app + " " + env + " " + version
}
