package com.indix.mesos

import java.net.SocketTimeoutException

import com.google.common.io.BaseEncoding

import scala.collection.mutable
import scalaj.http._

case class GOCDPoller(server: String, user: String, password: String) {

  val authToken = BaseEncoding.base64().encode(s"${user}:${password}".getBytes("UTF-8"));

  val responseHistory: scala.collection.mutable.MutableList[Int] = mutable.MutableList.empty[Int]

  def goTaskQueueSize(): Int = {
    println("Polling GO Server for scheduled jobs")
    try {
      val response: HttpResponse[String] = Http(server + "go/api/jobs/scheduled.xml").header("Authorization", s"Basic ${authToken}").asString
      val responseXml = scala.xml.XML.loadString(response.body)
      return (responseXml \ "scheduled").size
    } catch {
      case e: SocketTimeoutException => {
        println("GOCD Server timed out!!")
        return 0
      }
    }
  }

  def goIdleAgentsCount() = {
  }

  def pollAndAddTask() = {
    val scheduled : Int = goTaskQueueSize()
    println(s"Go server has ${scheduled.toString} pending jobs to be scheduled")
    if(scheduled > 0)
      responseHistory += scheduled
    
    if(responseHistory.size > 5) {
      println(s"More than 5 jobs pending in the GOCD. queuing a new agent launch now.")
      TaskQueue.enqueue(GoTask("./install_go_agent.sh", "", "https://raw.githubusercontent.com/ind9/gocd-mesos/master/bin/install_goagent.sh"))
      responseHistory.clear()
    }
  }

}
