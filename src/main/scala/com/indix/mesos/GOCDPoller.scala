package com.indix.mesos

import java.net.{UnknownHostException, SocketTimeoutException}

import com.google.common.io.BaseEncoding

import scala.collection.mutable
import scalaj.http._
import play.api.libs.json._

case class GOCDPoller(conf: FrameworkConfig) {

  val authToken = BaseEncoding.base64().encode(s"${conf.goUserName}:${conf.goPassword}".getBytes("UTF-8"));

  val responseHistory: scala.collection.mutable.MutableList[Int] = mutable.MutableList.empty[Int]


  private[mesos] def request(url: String) = {
    val request = if(conf.goAuthEnabled) {
      Http(url)
        .header("Authorization", s"Basic $authToken")
    } else {
      Http(url)
    }.header("Accept", "application/vnd.go.cd.v1+json")
    request.asString.body
  }

  private[mesos] def jsonRequest(url: String) = {
    Json.parse(request(url))
  }

  private[mesos] def xmlRequest(url: String) = {
    scala.xml.XML.loadString(request(url))
  }

  private def buildUrl() = {
    s"http://${conf.goServerHost}:${conf.goServerPort}"
  }


  def goTaskQueueSize(): Int = {
    println("Polling GO Server for scheduled jobs")
    try {
      val responseXml = xmlRequest(buildUrl() + "/go/api/jobs/scheduled.xml")
      (responseXml \\ "scheduledJobs" \\ "job").size
    } catch {
      case e: SocketTimeoutException => {
        println("GOCD Server timed out!!")
        0
      }
    }
  }

  def goIdleAgentsCount() = {
    println("Polling Go server for idle agents")
    try {
      val responseJson = jsonRequest(buildUrl() + "/go/api/agents")
      (responseJson \ "_embedded" \ "agents").toOption.map(jsValue => {
        jsValue.as[JsArray].value.map(agent => (agent \ "status").get.as[String]).count(_.equalsIgnoreCase("idle"))
      }).getOrElse(0)
    } catch {
      case e: SocketTimeoutException => {
        println("GOCD Server timed out!!")
        1
      }
    }
  }

  // Return true when the demand remains, same or increased during the last five attempts.
  private[mesos] def isDemandPositive(): Boolean = {
    val demandTrend = responseHistory.foldRight(0)((h1, h2) => {
     if(h2 >= h1) {
       1
     } else {
       -1
     }
    })
    demandTrend == 1
  }

  def pollAndAddTask() = {
    val scheduled : Int = goTaskQueueSize()
    println(s"Go server has ${scheduled.toString} pending jobs to be scheduled")
    if(scheduled > 0) {
      responseHistory += scheduled
    }
    if(responseHistory.size > 5) {
      println(s"More than 5 jobs pending in the GOCD. checking if any agents are idle.")
      if(goIdleAgentsCount() == 0 && isDemandPositive()) {
        println("No idle agents found. Launching a new agent launch now.")
        TaskQueue.enqueue(GoTask("", conf.goAgentDocker, ""))
        responseHistory.clear()
      } else {
        println("Idle agents found. Not launching any new Go Agent now.")
        responseHistory.drop(0)
      }
    }
  }
}
