package com.indix.mesos

import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.apache.mesos.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.Protos.Environment.Variable
import org.apache.mesos.Protos._
import org.apache.mesos.{MesosSchedulerDriver, Protos, Scheduler, SchedulerDriver}

import scala.collection.JavaConverters._
import scala.concurrent.duration._


class GoCDScheduler(conf : FrameworkConfig) extends Scheduler {


  lazy val envForGoCDTask = Environment.newBuilder()
    .addVariables(Variable.newBuilder().setName("GO_SERVER").setValue(conf.goServerHost).build())
    .addVariables(Variable.newBuilder().setName("AGENT_KEY").setValue(conf.goAgentKey.getOrElse(UUID.randomUUID().toString)).build())

  override def error(driver: SchedulerDriver, message: String) {}

  override def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {
    println(s"executor completed execution with status: $status")
  }

  override def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  override def disconnected(driver: SchedulerDriver): Unit = {
    println(s"Received Disconnected message $driver")
  }

  override def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {}

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
    println(s"Received resource offer size: ${offers.size()}")
    //for every available offer run tasks
    for (offer <- offers.asScala) {
      println(s"offer $offer")
      val nextTask = TaskQueue.dequeue
      if(nextTask != null) {
        val task = deployGoAgentTask(nextTask, offer)
        task match {
          case Some(tt) => driver.launchTasks(List(offer.getId).asJava, List(tt).asJava)
          case None => {
            TaskQueue.enqueue(nextTask)
            println(s"declining unused offer because offer is not enough")
            driver.declineOffer(offer.getId)
          }
        }
      }
      else {
        println(s"declining unused offer because there is no task")
        driver.declineOffer(offer.getId)
      }
    }
  }

  private def resource(name: String, value: Double) = {
    Resource.newBuilder()
      .setType(Protos.Value.Type.SCALAR)
      .setName(name)
      .setScalar(Value.Scalar.newBuilder().setValue(value))
      .build
  }

  private def dockerInfo(goTask: GoTask) = {
    Protos.ContainerInfo.DockerInfo
      .newBuilder()
      .setImage(goTask.dockerImage)
      .setNetwork(DockerInfo.Network.BRIDGE)
  }

  private def dockerContainerInfo(goTask: GoTask) = {
    Protos.ContainerInfo.newBuilder()
      .setType(Protos.ContainerInfo.Type.DOCKER)
      .setDocker(dockerInfo(goTask).build())
  }

  private def executorInfo(goTask: GoTask, currentTimeStamp: Long) = {
   Protos.ExecutorInfo.newBuilder()
      .setExecutorId(ExecutorID.newBuilder().setValue("gocd-agent-executor-" + currentTimeStamp))
      .setName("GOCD-Agent-Executor")
      .setContainer(dockerContainerInfo(goTask).build())
      .setCommand(Protos.CommandInfo.newBuilder()
        .setEnvironment(Protos.Environment.newBuilder()
          .addAllVariables(envForGoCDTask.getVariablesList))
        .setShell(false))
  }

  
  def deployGoAgentTask(goTask: GoTask, offer: Offer) =  {
    val needed = Resources(goTask)
    val available = Resources(offer)
    if(available.canSatisfy(needed)) {
      val currentTimeStamp = System.currentTimeMillis()
      val taskId = "gocd-agent-task-" + currentTimeStamp
      val task = TaskInfo.newBuilder
           .setExecutor(executorInfo(goTask, currentTimeStamp).build())
           .setName(taskId)
           .setTaskId(TaskID.newBuilder.setValue(taskId))
       task
        .addResources(resource("cpus", needed.cpus))
        .addResources(resource("mem", needed.memory))
        .setSlaveId(offer.getSlaveId)
      Some(task.build())
    } else {
      None
    }
  }

  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {
    println(s"RE-registered with mesos master.")
  }

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {
    println(s"registered with mesos master. Framework id is ${frameworkId.getValue}")
  }

}


  object GoCDMesosFramework extends App {
   val config = new FrameworkConfig(ConfigFactory.load())

    val id = "GOCD-Mesos-" + System.currentTimeMillis()


    println(s"The Framework id is $id")


    val poller = GOCDPoller(config)
    val timeInterval = 2 * 60 * 1000
    val runnable = new Runnable {
      override def run(): Unit = {
        while(true) {
          poller.pollAndAddTask
          Thread.sleep(timeInterval)
        }
      }
    }

    val frameworkInfo = FrameworkInfo.newBuilder()
      .setId(FrameworkID.newBuilder().setValue(id).build())
      .setName("GOCD-Mesos")
      .setUser("")
      .setRole("*")
      .setHostname("pattigai")
      .setCheckpoint(true)
      .setFailoverTimeout(60.seconds.toMillis)
      .build()

    val thread = new Thread(runnable)
    thread.start()
    val scheduler = new GoCDScheduler(config)
    val driver = new MesosSchedulerDriver(scheduler, frameworkInfo, config.mesosMaster)
    println("Starting the driver")

    val status = if(driver.run() == Status.DRIVER_STOPPED)  0 else 1

    //driver.run }

   // println(driver.join())

    println(status)
    // Ensure that the driver process terminates.
    driver.stop();
    // For this test to pass reliably on some platforms, this sleep is
    // required to ensure that the SchedulerDriver teardown is complete
    // before the JVM starts running native object destructors after
    // System.exit() is called. 500ms proved successful in test runs,
    // but on a heavily loaded machine it might not.
    // TODO(greg): Ideally, we would inspect the status of the driver
    // and its associated tasks via the Java API and wait until their
    // teardown is complete to exit.
    Thread.sleep(500);

    System.exit(status);
    println("===================================")
}