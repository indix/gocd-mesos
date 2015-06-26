package com.indix.mesos

import scala.collection.mutable
import scalaj.http._

case class GOCDPoller(server: String, user: String, password: String) {

  val authToken = Base64.encode(s"Basic $user:$password".getBytes).toString

  var responseHistory: scala.collection.mutable.MutableList[Int] = mutable.MutableList.empty[Int]

  def goTaskQueueSize() = {
    val response: HttpResponse[String] = Http(server).header("Authorization", authToken).asString
    val responseXml = scala.xml.XML.loadString(response.body)
    (responseXml \ "scheduled").size
  }

  def goIdleAgentsCount() = {
  }

  def pollAndAddTask() = {
    val scheduled : Int = goTaskQueueSize()
    if(scheduled > 0)
      responseHistory += scheduled
    
    if(responseHistory.size > 5) {
      TaskQueue.enqueue(GoTask("./install_go_agent.sh", "", "https://raw.githubusercontent.com/ind9/gocd-mesos/master/bin/install_goagent.sh"))
      responseHistory.clear()
    }
  }

}
