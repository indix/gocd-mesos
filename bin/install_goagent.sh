#!/bin/bash

# Args: GoCD Server URI (http://build.indix.tv:8080/ )
# 
# Download Go agent from repo.indix.tv
# rpm install go-agent-15.1.0
#
echo "# Following ENV Variables are needed for this script to work"
echo "# REPO_USER"
echo "# REPO_PASSWD"
echo "# AGENT_PACKAGE_URL"
echo "# GOCD_SERVER"

AGENT_RPM=`echo ${AGENT_PACKAGE_URL} | awk -F"/" '{print $NF}'`

curl -u ${REPO_USER}:${REPO_PASSWD} -O ${AGENT_PACKAGE_URL}

echo "Installing go agent ${AGENT_RPM}"
sudo rpm -ivh ${AGENT_RPM}

echo "Setting up agent to talk to Go server @ ${GOCD_SERVER}"
sudo sed -e "s#GO_SERVER=.*#GO_SERVER=${GOCD_SERVER}#g" -i /etc/default/go-agent

GUID_FILE="/var/lib/go-agent/config/guid.txt"
echo "Setting guid for Agent"
echo "$GUID" > ${GUID_FILE}

echo "Starting Go agent"
sudo /etc/init.d/go-agent restart

is_go_running=`ps -aef | grep go-agent`
echo "Checking to see if Go Agent is running"
echo ${is_go_running}

echo "Registering go-agent against the GO Server"
curl -u 'indix:1nd1x!@#$%' http://build.indix.tv:8080/go/api/agents/${GUID}/enable

echo "Listing All Go agents on the Server"
curl -u 'indix:1nd1x!@#$%' http://build.indix.tv:8080/go/api/agents

## Debug stuff!
read
