package com.indix.mesos

import java.net.{UnknownHostException, SocketTimeoutException}

import com.google.common.io.BaseEncoding

import scala.collection.mutable
import scalaj.http._

case class GOCDPoller(conf: FrameworkConfig) {

  val authToken = BaseEncoding.base64().encode(s"${conf.goUserName}:${conf.goPassword}".getBytes("UTF-8"));

  val responseHistory: scala.collection.mutable.MutableList[Int] = mutable.MutableList.empty[Int]

  def goTaskQueueSize(): Int = {
    println("Polling GO Server for scheduled jobs")
    try {
      val response: HttpResponse[String] = Http(s"http://${conf.goServerHost}:${conf.goServerPort}" + "/go/api/jobs/scheduled.xml").asString //.header("Authorization", s"Basic ${authToken}").asString
      val responseXml = scala.xml.XML.loadString(response.body)
      (responseXml \\ "scheduledJobs" \\ "job").size
    } catch {
      case e: SocketTimeoutException => {
        println("GOCD Server timed out!!")
        0
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
      TaskQueue.enqueue(GoTask("", conf.goAgentDocker, ""))
      responseHistory.clear()
    }
  }

}
