package com.indix.mesos


import scalaj.http._

case class GOCDPoller(server: String, user: String, password: String) {

  val authToken = Base64.encode(s"Basic ${user}:${password}".getBytes)

  var responseHistory: List[Int] = List.empty

  def poll() = {
    val response: HttpResponse[String] = Http(server).header("Authorization", authToken).asString
    val responseXml = scala.xml.XML.loadString(response.body)
    (responseXml \ "scheduled").size
  }

  def pollAndAddTask = {
    val scheduled : List[String] = poll
    responseHistory = responseHistory ++ scheduled.size
    if(responseHistory.size > 5)
      if(!responseHistory.exists(x => x <= 1)) {
        TaskQueue.enqueue(GoTask("./install_go_agent.sh", "", "https://raw.githubusercontent.com/ind9/gocd-mesos/master/bin/install_goagent.sh"))
        responseHistory = List.empty
      }
      else
        responseHistory = responseHistory.drop(0)
  }

}
