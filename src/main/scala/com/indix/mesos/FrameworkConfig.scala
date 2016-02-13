package com.indix.mesos

import com.typesafe.config.Config

class FrameworkConfig(config: Config) {
    val mesosMaster = config.getString("mesos-master")

    val goAgentDocker = config.getString("go-agent-docker")

    val goServerHost = config.getString("go-server-host")

    val goServerPort = config.getString("go-server-port")

    val goUserName = config.getString("go-user-name")
    val goPassword = config.getString("go-password")

    val goAgentKey = if(config.hasPath("go-agent-key")) Some(config.getString("go-agent-key")) else None
}
