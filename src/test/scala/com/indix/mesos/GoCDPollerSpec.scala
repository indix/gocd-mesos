//package com.indix.mesos
//
//import com.typesafe.config.ConfigFactory
//import org.scalatest._
//import org.mockito.Mockito._
//import play.api.libs.json.Json
//
//class GoCDPollerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {
//
//
//  val poller = new GOCDPoller(new FrameworkConfig(ConfigFactory.load()))
//
//  override def beforeEach() = {
//    TaskQueue.reset
//  }
//
//  override def afterEach() = {
//    TaskQueue.reset
//  }
//
//  "GoCDPoller#pollAndAddTask" should "add a goTask after 5 polls" in {
//    // Given
//    val pollerSpy = spy(poller)
//    doReturn(1).when(pollerSpy).goTaskQueueSize()
//    doReturn(0).when(pollerSpy).goIdleAgentsCount()
//    doReturn(true).when(pollerSpy).isDemandPositive()
//
//
//    // When
//    for(i <- 0 to 5) {
//      pollerSpy.pollAndAddTask()
//    }
//
//    // Then
//    TaskQueue.queue.size() should be(1)
//  }
//
//  "GoCDPoller#pollAndAddTask" should "not add a goTask after 5 polls if Idle agents count is > 0" in {
//    // Given
//    val pollerSpy = spy(poller)
//    doReturn(1).when(pollerSpy).getPendingJobs()
//    doReturn(2).when(pollerSpy).goIdleAgentsCount()
//    doReturn(true).when(pollerSpy).isDemandPositive()
//
//    // When
//    for(i <- 0 to 5) {
//      pollerSpy.pollAndAddTask()
//    }
//
//    // Then
//    TaskQueue.queue.size() should be(0)
//  }
//
//  "GoCDPoller#pollAndAddTask" should "not add a goTask after 5 polls if isDemandPositive returns false" in {
//    // Given
//    val pollerSpy = spy(poller)
//    doReturn(1).when(pollerSpy).getPendingJobs()
//    doReturn(0).when(pollerSpy).goIdleAgentsCount()
//    doReturn(false).when(pollerSpy).isDemandPositive()
//
//    // When
//    for(i <- 0 to 5) {
//      pollerSpy.pollAndAddTask()
//    }
//
//    // Then
//    TaskQueue.queue.size() should be(0)
//  }
//
//
//
//  "GoCDPoller#pollAndAddTask" should "add a goTask after 15 polls" in {
//    // Given
//<<<<<<< Updated upstream
//    val pollerSpy = spy(poller)
//    doReturn(1).when(pollerSpy).goTaskQueueSize()
//    doReturn(0).when(pollerSpy).goIdleAgentsCount()
//    doReturn(true).when(pollerSpy).isDemandPositive()
//
//=======
//    val pollerSpy = spy(poller);
//    doReturn(1).when(pollerSpy).getPendingJobs()
//>>>>>>> Stashed changes
//
//    // When
//    for(i <- 0 to 20) {
//      pollerSpy.pollAndAddTask()
//    }
//
//    // Then
//    TaskQueue.queue.size() should be(4)
//  }
//
//  "GoCDPoller#pollAndAddTask" should "add a goTask with expected attributes" in {
//    // Given
//<<<<<<< Updated upstream
//    val pollerSpy = spy(poller)
//    doReturn(1).when(pollerSpy).goTaskQueueSize()
//    doReturn(0).when(pollerSpy).goIdleAgentsCount()
//    doReturn(true).when(pollerSpy).isDemandPositive()
//
//=======
//    val pollerSpy = spy(poller);
//    doReturn(1).when(pollerSpy).getPendingJobs()
//>>>>>>> Stashed changes
//
//    // When
//    for(i <- 0 to 5) {
//      pollerSpy.pollAndAddTask()
//    }
//
//    // Then
//    TaskQueue.queue.size() should be(1)
//    TaskQueue.find.dockerImage should be ("travix/gocd-agent:latest")
//  }
//
//  "GoCDPoller#goIdleAgentsCount" should "fetch idle agents count given a valid json" in {
//    val responseJson = Json.parse(
//      """
//        |
//        |{
//        |  "_links": {
//        |    "self": {
//        |      "href": "https://ci.example.com/go/api/agents"
//        |    },
//        |    "doc": {
//        |      "href": "http://api.go.cd/#agents"
//        |    }
//        |  },
//        |  "_embedded": {
//        |    "agents": [
//        |      {
//        |        "_links": {
//        |          "self": {
//        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
//        |          },
//        |          "doc": {
//        |            "href": "http://api.go.cd/#agents"
//        |          },
//        |          "find": {
//        |            "href": "https://ci.example.com/go/api/agents/:uuid"
//        |          }
//        |        },
//        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
//        |        "hostname": "agent01.example.com",
//        |        "ip_address": "10.12.20.47",
//        |        "enabled": true,
//        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
//        |        "status": "Idle",
//        |        "operating_system": "Mac OS X",
//        |        "free_space": 84983328768,
//        |        "resources": ["java", "linux", "firefox"],
//        |        "environments": ["perf", "UAT"]
//        |      }
//        |    ]
//        |  }
//        |}
//        |
//      """.stripMargin)
//    // Given
//    val pollerSpy = spy(poller)
//    doReturn(responseJson).when(pollerSpy).jsonRequest("http://localhost:8080/go/api/agents")
//
//    // When, then
//    pollerSpy.goIdleAgentsCount() should be(1)
//  }
//
//  "GoCDPoller#goTaskQueueSize" should "fetch pending jobs count given a valid json" in {
//    val responseJson =
//      """
//        |<scheduledJobs>
//        |  <job name="job1" id="6">
//        |    <link rel="self" href="https://ci.example.com/go/tab/build/detail/mypipeline/5/defaultStage/1/job1"/>
//        |    <buildLocator>mypipeline/5/defaultStage/1/job1</buildLocator>
//        |  </job>
//        |  <job name="job2" id="7">
//        |    <link rel="self" href="https://ci.example.com/go/tab/build/detail/mypipeline/5/defaultStage/1/job2"/>
//        |    <buildLocator>mypipeline/5/defaultStage/1/job2</buildLocator>
//        |  </job>
//        |</scheduledJobs>
//      """.stripMargin
//    // Given
//    val pollerSpy = spy(poller)
//    doReturn(responseJson).when(pollerSpy).request("http://localhost:8080/go/api/jobs/scheduled.xml")
//
//    // When, then
//    pollerSpy.getPendingJobs() should be(2)
//  }
//}
