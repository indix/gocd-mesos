package com.indix.mesos

import com.typesafe.config.ConfigFactory
import org.apache.mesos.Protos.CommandInfo.URI
import org.apache.mesos.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.Protos.Environment.Variable
import org.apache.mesos.Protos._
import org.apache.mesos.{MesosSchedulerDriver, Scheduler, SchedulerDriver}

import scala.collection.JavaConverters._



case class Resources(cpus: Double, memory: Double, disk: Double) {
  def canSatifsy(another: Resources) {
    this.cpus >= another.cpus && this.memory >= another.memory && this.cpus >= another.memory
  }
}


object Resources {
  def apply(task: GoTask): Resources =  {
    new Resources(task.taskInfo.getOrElse("cpus", 1),
    task.taskInfo.getOrElse("mem", 256),
    task.taskInfo.getOrElse("disk", 0))
  }



  def apply(offer: Offer): Resources = {
    val resources = offer.getResourcesList.asScala
    new Resources(
      resources.find(_.getName == "cpus").map(x => x.getScalar.getValue).getOrElse(1),
      resources.find(_.getName == "mem").map(x => x.getScalar.getValue).getOrElse(256),
      resources.find(_.getName == "disk").map(x => x.getScalar.getValue).getOrElse(0))
  }
}

class GoCDScheduler(
                    conf : FrameworkConfig
                    ) extends Scheduler {

  override def error(driver: SchedulerDriver, message: String) {
  }

  override def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {
    println(s"executor completed execution with status: $status")
  }

  override def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  override def disconnected(driver: SchedulerDriver) {}

  override def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {

  }

  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    println(s"received status update $status")
  }

  override def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {}



  /**
   *
   * This callback is called when resources are available to  run tasks
   *
   */
  override def resourceOffers(driver: SchedulerDriver, offers: java.util.List[Offer]) {

    //for every available offer run tasks
    for (offer <- offers.asScala) {
      println(s"offer $offer")
      val nextTask = TaskQueue.dequeue
      val task: GoTask = buildMesosTask(nextTask, offer)
      if(task == null) {
        // return un used resources, as a good citizen
        TaskQueue.enqueue(task)
        driver.get().declineOffer(offer.getId)
      }
      driver.launchTasks(List(offer.getId), List(task).asJava)
    }
  }

  def buildMesosTask(goTask: GoTask, offer: Offer): TaskInfo =  {

    val needed = Resources(goTask)

    val available = Resources(offer)


    if(available.canSatisfy(available)) {
      val id = "task" + System.currentTimeMillis()

      //create task with given command
      val task = TaskInfo.newBuilder
        .setCommand(
          CommandInfo
            .newBuilder()
            .setValue(goTask.cmdString)
            .addUris(URI.newBuilder().setValue(goTask.uri).build())
            .setEnvironment(Environment.newBuilder()
                .addVariables(Variable.newBuilder().setName("GOCD_SERVER").setValue(conf.mesosMaster).build())
                .addVariables(Variable.newBuilder().setName("REPO_USER").setValue(conf.goUserName).build())
                .addVariables(Variable.newBuilder().setName("REPO_PASSWD").setValue(conf.goPassword).build())
                .addVariables(Variable.newBuilder().setName("AGENT_PACKAGE_URL").setValue(conf.goAgentBinary).build())
              .build)
            .build)
        .setName(id)
        .setTaskId(TaskID.newBuilder.setValue(id))
        .setContainer(ContainerInfo.newBuilder()
          .setType(ContainerInfo.Type.DOCKER)
          .setDocker(DockerInfo.newBuilder()
            .setImage(goTask.dockerImage)
            .build
         ).build)
        .addResources(Resource.newBuilder().setName("cpus").setScalar(Value.Scalar.newBuilder().setValue(needed.cpus)).build)
        .addResources(Resource.newBuilder().setName("mem").setScalar(Value.Scalar.newBuilder().setValue(needed.memory)).build)
        .setSlaveId(offer.getSlaveId)
        .build
      return task
    } else {
      return null
    }
  }


  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {}

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {
  }

}


object GoCDMesosFramework extends App {
  val config = new FrameworkConfig(ConfigFactory.load())
  val framework = FrameworkInfo("GOCD-Mesos")

  val scheduler = new GoCDScheduler(config)
  val driver = new MesosSchedulerDriver(scheduler, framework.toProto, config.mesosMaster)
  driver.run();

}