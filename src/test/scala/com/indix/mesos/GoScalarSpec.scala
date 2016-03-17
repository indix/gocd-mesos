package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.apache.mesos.Protos._
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkID
import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent.duration._


class GoScalarSpec extends FlatSpec with Matchers {
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

  "GoCDScalar#computeScaledown" should "return correct number to scale up" in {
    scalar.computeScaledown(9, 6, 12, 2) should be (3)
    scalar.computeScaledown(14, 13, 12, 2) should be (2)
    scalar.computeScaledown(4, 0, 12, 2) should be (2)
    scalar.computeScaledown(12, 0, 12, 2) should be (10)
    scalar.computeScaledown(12, 1, 12, 2) should be (10)
  }
}
