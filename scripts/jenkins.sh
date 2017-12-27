#!/bin/sh
#export JENKINS_HOME=/export/home/jenkins/jenkins_home
   
cmd_start() {
  echo "Starting Jenkins CI Server"
  # --httpsKeyStore=/export/home/jenkins/the.local.jks --httpsKeyStorePassword=xxx
  nohup java -jar ./jenkins.war --httpPort=-1 --httpsPort=8443 > ./jenkins.log 2>&1 & echo $! > run.pid

}

cmd_stop() {
  echo "Stopping Jenkins CI Server"
  if [ -r ./run.pid ]; then
    read -r JENKINS_PID < ./run.pid
    kill -9 ${JENKINS_PID}  
    rm ./run.pid
    echo "Jenkins stopped."
  else
    echo "Jenkins instance not found."
  fi
}
 
case $1 in
    start)
    cmd_start
    ;;
    stop)
    cmd_stop
    ;;
    restart)
    echo -n "Jenkins CI Server"
    cmd_stop
    sleep 1
    cmd_start
    ;;
    *)
    echo "usage: jenkins.sh {start|stop|restart}"
    exit 1
    ;;
esac
 
exit 0
