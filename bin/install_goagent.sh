#!/bin/bash

# Args: GoCD Server URI (http://build.indix.tv:8080/ )
# 
# Download Go agent from repo.indix.tv
# rpm install go-agent-15.1.0
#
if [ $# -ne 1 ]; then
	echo "Needs one argument. The go server to which this agent should talk to"
	echo "Pl ensure the following env variables are exported in the shell this script is being run"
	echo "REPO_USER => Indix repo user name"
	echo "REPO_PASSWD => Indix repo password"
	echo "AGENT_PACKAGE_URL => The rpm package url"
	exit -1
fi

GOCD_SERVER=$1

echo "# Following ENV Variables are needed for this script to work"
echo "# REPO_USER"
echo "# REPO_PASSWD"
echo "# AGENT_PACKAGE_URL"

AGENT_RPM=`echo ${AGENT_PACKAGE_URL} | awk -F"/" '{print $NF}'`

curl -u ${REPO_USER}:${REPO_PASSWD} -O ${AGENT_PACKAGE_URL}

echo "Installing go agent ${AGENT_RPM}"
sudo rpm -ivh ${AGENT_RPM}

echo "Setting up agent to talk to Go server @ ${GOCD_SERVER}"
sudo sed -e "s#GO_SERVER=.*#GO_SERVER=${GOCD_SERVER}#g" -i /etc/default/go-agent

echo "Starting Go agent"
sudo /etc/init.d/go-agent restart
