package com.indix.mesos

import org.apache.mesos.Protos.Offer
import scala.collection.JavaConverters._


case class Resources(cpus: Double, memory: Double, disk: Double) {
  def canSatisfy(another: Resources): Boolean = {
    this.cpus >= another.cpus && this.memory >= another.memory
  }
}
object Resources {
  def apply(task: GoTask): Resources =  {
    println(task.resource.cpu, task.resource.memory)
    new Resources(task.resource.cpu,
      task.resource.memory,
      task.resource.disk)
  }



  def apply(offer: Offer): Resources = {
    val resources = offer.getResourcesList.asScala
    new Resources(
      resources.find(_.getName == "cpus").map(x => x.getScalar.getValue).getOrElse(1),
      resources.find(_.getName == "mem").map(x => x.getScalar.getValue).getOrElse(256),
      resources.find(_.getName == "disk").map(x => x.getScalar.getValue).getOrElse(0))
  }
}
