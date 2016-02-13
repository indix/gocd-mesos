package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.apache.mesos.Protos
import org.apache.mesos.Protos.Value.Scalar
import org.apache.mesos.Protos.{Resource, Offer}
import org.scalatest.{Matchers, FlatSpec}


class GoCDSchedulerTest extends FlatSpec with Matchers {
  val scheduler = new GoCDScheduler(new FrameworkConfig(ConfigFactory.load()))



  def generoursResourceOffer: Offer = {
    Offer
      .newBuilder()
      .addResources(Resource.newBuilder()
        .setName("cpu")
        .setType(Protos.Value.Type.SCALAR)
        .setScalar(Scalar.newBuilder().setValue(2).build())
        .build())
      .addResources(Resource.newBuilder()
        .setName("mem")
        .setType(Protos.Value.Type.SCALAR)
        .setScalar(Scalar.newBuilder().setValue(512).build())
        .build())
      .setId(Protos.OfferID.newBuilder().setValue("inadequate_offer"))
      .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("imaginary_framework"))
      .setSlaveId(Protos.SlaveID.newBuilder().setValue("nonexistent_slave"))
      .setHostname("localhost")
      .build()
  }

  def inadequateOffer: Offer = {
    Offer
      .newBuilder()
      .addResources(Resource.newBuilder()
        .setName("cpu")
        .setType(Protos.Value.Type.SCALAR)
        .setScalar(Scalar.newBuilder().setValue(0.1).build())
        .build())
      .addResources(Resource.newBuilder()
        .setName("mem")
        .setType(Protos.Value.Type.SCALAR)
        .setScalar(Scalar.newBuilder().setValue(0.1).build())
        .build())
      .setId(Protos.OfferID.newBuilder().setValue("inadequate_offer"))
      .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("imaginary_framework"))
      .setSlaveId(Protos.SlaveID.newBuilder().setValue("nonexistent_slave"))
      .setHostname("localhost")
      .build()
  }

  "GoCDScheduler#deployGoAgentTask" should "refuse offer if there are not enough resources" in {
    scheduler.deployGoAgentTask(GoTask("", "docker-image", ""), inadequateOffer) should be(None)
  }

  "GOCDScheduler#deployGoAgentTask" should "accept offer if there are enough resources" in {
    val taskOpt = scheduler.deployGoAgentTask(GoTask("", "docker-image",""), generoursResourceOffer)
    taskOpt.isDefined should be(true)
    taskOpt.get.getExecutor.getContainer.getDocker.getImage should be("docker-image")
  }
}
