package com.indix.mesos

import com.google.common.io.BaseEncoding
import scala.collection.mutable
import scala.util.{Failure, Success, Try}
import scalaj.http._
import play.api.libs.json._


case class GoAgent(id: String, status: String)

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


  def getPendingJobsCount: Int = {
    println("Polling GO Server for scheduled jobs")
     withRetry(3){
       val responseXml = xmlRequest(buildUrl() + "/go/api/jobs/scheduled.xml")
       (responseXml \\ "scheduledJobs" \\ "job").size
      }.getOrElse(0)
  }

  def getAllAgents: List[GoAgent] = {
    println("Polling Go server for agents")
    withRetry(3) {
      val responseJson = jsonRequest(buildUrl() + "/go/api/agents")
      (responseJson \ "_embedded" \ "agents").toOption.map(jsValue => {
        jsValue.as[JsArray]
          .value
          .map(agent => GoAgent((agent \ "uuid").get.as[String], (agent \ "status").get.as[String]))
          .toList
      }).getOrElse(List.empty)
    }.getOrElse(List.empty)
  }

  def getIdleAgents: List[GoAgent] = {
    getAllAgents.filter(_.status.equalsIgnoreCase("idle"))
  }

  def getBuildingAgents = {
    getAllAgents.filter(_.status.equalsIgnoreCase("building"))
  }



  private def withRetry[T](n: Int)(fn: => T): Try[T] = {
   Try(fn) match {
     case res: Success[T] => res
     case _ if n < 1 => withRetry(n - 1)(fn)
     case Failure(ex) => throw ex
   }
  }
}
