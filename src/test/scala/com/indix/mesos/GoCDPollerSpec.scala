package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.mockito.Mockito._
import play.api.libs.json.Json

class GoCDPollerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {


  val poller = new GOCDPoller(new FrameworkConfig(ConfigFactory.load()))

  override def beforeEach() = {
    TaskQueue.reset
  }

  override def afterEach() = {
    TaskQueue.reset
  }

  "GoCDPoller#goIdleAgents" should "fetch idle agents count given a valid json" in {
    val responseJson = Json.parse(
      """
        |
        |{
        |  "_links": {
        |    "self": {
        |      "href": "https://ci.example.com/go/api/agents"
        |    },
        |    "doc": {
        |      "href": "http://api.go.cd/#agents"
        |    }
        |  },
        |  "_embedded": {
        |    "agents": [
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      }
        |    ]
        |  }
        |}
        |
      """.stripMargin)
    // Given
    val pollerSpy = spy(poller)
    doReturn(responseJson).when(pollerSpy).jsonRequest("http://localhost:8080/go/api/agents")

    // When, then
    pollerSpy.getIdleAgents.size should be(1)
  }

  "GoCDPoller#getAllAgents" should "fetch all agents count given a valid json" in {
    val responseJson = Json.parse(
      """
        |
        |{
        |  "_links": {
        |    "self": {
        |      "href": "https://ci.example.com/go/api/agents"
        |    },
        |    "doc": {
        |      "href": "http://api.go.cd/#agents"
        |    }
        |  },
        |  "_embedded": {
        |    "agents": [
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      },
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      },
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      }
        |    ]
        |  }
        |}
        |
      """.stripMargin)
    // Given
    val pollerSpy = spy(poller)
    doReturn(responseJson).when(pollerSpy).jsonRequest("http://localhost:8080/go/api/agents")

    // When, then
    pollerSpy.getAllAgents.size should be(3)
  }

  "GoCDPoller#getBuildingAgents" should "fetch building agents count given a valid json" in {
    val responseJson = Json.parse(
      """
        |
        |{
        |  "_links": {
        |    "self": {
        |      "href": "https://ci.example.com/go/api/agents"
        |    },
        |    "doc": {
        |      "href": "http://api.go.cd/#agents"
        |    }
        |  },
        |  "_embedded": {
        |    "agents": [
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      },
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Building",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      },
        |      {
        |        "_links": {
        |          "self": {
        |            "href": "https://ci.example.com/go/api/agents/adb9540a-b954-4571-9d9b-2f330739d4da"
        |          },
        |          "doc": {
        |            "href": "http://api.go.cd/#agents"
        |          },
        |          "find": {
        |            "href": "https://ci.example.com/go/api/agents/:uuid"
        |          }
        |        },
        |        "uuid": "adb9540a-b954-4571-9d9b-2f330739d4da",
        |        "hostname": "agent01.example.com",
        |        "ip_address": "10.12.20.47",
        |        "enabled": true,
        |        "sandbox": "/Users/ketanpadegaonkar/projects/gocd/gocd/agent",
        |        "status": "Idle",
        |        "operating_system": "Mac OS X",
        |        "free_space": 84983328768,
        |        "resources": ["java", "linux", "firefox"],
        |        "environments": ["perf", "UAT"]
        |      }
        |    ]
        |  }
        |}
        |
      """.stripMargin)
    // Given
    val pollerSpy = spy(poller)
    doReturn(responseJson).when(pollerSpy).jsonRequest("http://localhost:8080/go/api/agents")

    // When, then
    pollerSpy.getBuildingAgents.size should be(1)
  }

  "GoCDPoller#getPendingJobsCount" should "fetch pending jobs count given a valid json" in {
    val responseJson =
      """
        |<scheduledJobs>
        |  <job name="job1" id="6">
        |    <link rel="self" href="https://ci.example.com/go/tab/build/detail/mypipeline/5/defaultStage/1/job1"/>
        |    <buildLocator>mypipeline/5/defaultStage/1/job1</buildLocator>
        |  </job>
        |  <job name="job2" id="7">
        |    <link rel="self" href="https://ci.example.com/go/tab/build/detail/mypipeline/5/defaultStage/1/job2"/>
        |    <buildLocator>mypipeline/5/defaultStage/1/job2</buildLocator>
        |  </job>
        |</scheduledJobs>
      """.stripMargin
    // Given
    val pollerSpy = spy(poller)
    doReturn(responseJson).when(pollerSpy).request("http://localhost:8080/go/api/jobs/scheduled.xml")

    // When, then
    pollerSpy.getPendingJobsCount should be(2)
  }
}
