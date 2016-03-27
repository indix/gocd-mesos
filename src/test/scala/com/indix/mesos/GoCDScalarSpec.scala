package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.apache.mesos.Protos._
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkID
import org.mockito.Mockito._
import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent.duration._


class GoCDScalarSpec extends FlatSpec with Matchers {
  val conf = new FrameworkConfig(ConfigFactory.load())
  val scheduler = new GoCDScheduler(conf)
  val poller = new GOCDPoller(conf)
  val frameworkInfo = FrameworkInfo.newBuilder()
    .setId(FrameworkID.newBuilder().setValue("").build())
    .setName("GOCD-Mesos")
    .setUser("")
    .setRole("*")
    .setHostname("pattigai")
    .setCheckpoint(true)
    .setFailoverTimeout(60.seconds.toMillis)
    .build()
  val driver = new MesosSchedulerDriver(scheduler, frameworkInfo, "")

  val scalar = new GOCDScalar(conf, poller, driver)

  "GoCDScalar#computeScaledown" should "return correct number to scale down" in {
    scalar.computeScaledown(9, 6, 12, 2) should be (3)
    scalar.computeScaledown(14, 13, 12, 2) should be (2)
    scalar.computeScaledown(4, 0, 12, 2) should be (2)
    scalar.computeScaledown(12, 0, 12, 2) should be (10)
    scalar.computeScaledown(12, 1, 12, 2) should be (10)
  }

  "GoCDScalar#computeScaleUp" should "return correct number to scale up" in {
    scalar.computeScaleup(6, 9, 12, 2) should be (3)
    scalar.computeScaleup(13, 14, 12, 2) should be (0)
    scalar.computeScaleup(0, 4, 12, 2) should be (4)
    scalar.computeScaleup(0, 14, 12, 2) should be (12)
    scalar.computeScaleup(1, 12, 12, 2) should be (11)
  }

  "GoCDScalar#getSupply" should "return the supply metric" in {
    val pollerSpy = spy(poller)
    val scalar = new GOCDScalar(conf, pollerSpy, driver)
    val scalarSpy = spy(scalar)
    doReturn(Iterable(GoTask("", "", GoTaskState.Scheduled, "idle3", ""),  GoTask("", "", GoTaskState.Pending, "idle4", ""))).when(scalarSpy).pendingTasks
    doReturn(Iterable(GoTask("", "", GoTaskState.Running, "idle1", ""), GoTask("", "", GoTaskState.Running, "active1", ""))).when(scalarSpy).runningTasks
    doReturn(List(GoAgent("idle1", "Idle"))).when(pollerSpy).getIdleAgents
    scalarSpy.getSupply should be (3)
  }

  "GoCDScalar#getDemand" should "return the demand metric" in {
    val pollerSpy = spy(poller)
    val scalar = new GOCDScalar(conf, pollerSpy, driver)
    val scalarSpy = spy(scalar)
    doReturn(Iterable(GoAgent("active1", "Building"))).when(pollerSpy).getBuildingAgents
    doReturn(4).when(pollerSpy).getPendingJobsCount
    scalarSpy.getDemand should be (5)
  }
}
