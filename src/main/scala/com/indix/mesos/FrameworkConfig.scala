package com.indix.mesos

import com.typesafe.config.Config

class FrameworkConfig(config: Config) {

    val rootConfig = config.getConfig("gocd-mesos")

    val mesosMaster = rootConfig.getString("mesos-master")

    val goAgentDocker = rootConfig.getString("go-agent.docker-image")

    val goServerHost = rootConfig.getString("go-server.host")

    val goServerPort = rootConfig.getString("go-server.port")

    val goMinAgents = rootConfig.getInt("go-agent.min-agents")

    val goMaxAgents = rootConfig.getInt("go-agent.max-agents")

    lazy val goUserName = rootConfig.getString("go-server.user-name")
    lazy val goPassword = rootConfig.getString("go-server.password")

    val goAuthEnabled = if(rootConfig.hasPath("go-server.auth-enabled")) rootConfig.getBoolean("go-server.auth-enabled") else false

    val goAgentKey = if(rootConfig.hasPath("go-agent.auto-register-key")) Some(rootConfig.getString("go-agent.auto-register-key")) else None
}
