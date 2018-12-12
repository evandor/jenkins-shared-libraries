def call(app, env, version) {
    //sh "nohup /home/carsten/install/docker/services/run_docker.sh " + app + " " + env + " " + version
    sh "nohup /home/carsten/bin/dockerRun " + app + " " + env + " " + version
}
