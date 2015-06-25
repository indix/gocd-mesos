package com.indix.mesos

import com.typesafe.config.Config

class FrameworkConfig(config: Config) {
    val mesosMaster = config.getString("mesos-master")

    val goMasterDocker = config.getString("go-master-docker")

    val goAgentDocker = config.getString("go-agent-docker")

    val goMasterServer = config.getString("go-master-server")

    val goUserName = config.getString("go-user-name")
    val goPassword = config.getString("go-password")

    val goAgentBinary = config.getString("go-agent-binary")
    val repoUserName = config.getString("repo-user-name")
    val repoPassword = config.getString("repo-password")
}
