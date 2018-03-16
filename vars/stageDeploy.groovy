def call(app,version) {
    sh "nohup /home/carsten/install/docker/skysail/run_docker.sh "+app+" test 0.0.135"
}
