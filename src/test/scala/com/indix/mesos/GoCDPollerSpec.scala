package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.mockito.Mockito._

class GoCDPollerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {


  val poller = new GOCDPoller(new FrameworkConfig(ConfigFactory.load()))

  override def beforeEach() = {
    TaskQueue.reset
  }

  override def afterEach() = {
    TaskQueue.reset
  }

  "GoCDPoller#pollAndAddTask" should "add a goTask after 5 polls" in {
    // Given
    val pollerSpy = spy(poller);
    doReturn(1).when(pollerSpy).goTaskQueueSize()

    // When
    for(i <- 0 to 5) {
      pollerSpy.pollAndAddTask()
    }

    // Then
    TaskQueue.queue.size() should be(1)
  }


  "GoCDPoller#pollAndAddTask" should "add a goTask after 15 polls" in {
    // Given
    val pollerSpy = spy(poller);
    doReturn(1).when(pollerSpy).goTaskQueueSize()

    // When
    for(i <- 0 to 20) {
      pollerSpy.pollAndAddTask()
    }

    // Then
    TaskQueue.queue.size() should be(3)
  }

  "GoCDPoller#pollAndAddTask" should "add a goTask with expected attributes" in {
    // Given
    val pollerSpy = spy(poller);
    doReturn(1).when(pollerSpy).goTaskQueueSize()

    // When
    for(i <- 0 to 5) {
      pollerSpy.pollAndAddTask()
    }

    // Then
    TaskQueue.queue.size() should be(1)
    TaskQueue.dequeue.dockerImage should be ("travix/gocd-agent:latest")
  }
}
