package com.indix.mesos

import com.typesafe.config.Config

class FrameworkConfig(config: Config) {

    val rootConfig = config.getConfig("gocd-mesos")

    val mesosMaster = rootConfig.getString("mesos-master")

    val goAgentDocker = rootConfig.getString("go-agent.docker-image")

    val goServerHost = rootConfig.getString("go-server.host")

    val goServerPort = rootConfig.getString("go-server.port")

    val goUserName = rootConfig.getString("go-server.user-name")
    val goPassword = rootConfig.getString("go-server.password")

    val goAgentKey = if(rootConfig.hasPath("go-agent.auto-register-key")) Some(rootConfig.getString("go-agent.auto-register-key")) else None
}
