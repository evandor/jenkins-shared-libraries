#!/bin/sh
# This script will restore a backup stored in $JENKINS_BACKUP. You can give the file name as first argument. 
# If no argument is given, the latest backup will be restored.

jenkinsBackupFolder=~/jenkins_backup

./jenkins.sh stop
cd $jenkinsBackupFolder


if [ $# -eq 0 ]; then
  echo "No specific backup provided, latest backup will be restored."
  backupFilename=`ls -Art | tail -n 1`
else
  echo "Filename is set to $1."
  backupFilename=$1
fi

if [ ! -f $backupFilename ]; then
  echo "The $backupFilename file doesn't exist."
  exit 1
fi

tar xzvf $backupFilename
cp -R jenkins-backup/* $JENKINS_HOME
echo "Backup successful restored"
rm -rf jenkinsBackupFolder/jenkins-backup/
echo "Backup folder deleted"
echo "Please start Jenkins manually"
