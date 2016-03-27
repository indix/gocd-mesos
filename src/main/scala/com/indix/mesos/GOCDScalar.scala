package com.indix.mesos

import com.indix.mesos.common.GocdMesosLogger
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.{TaskState, TaskID, TaskStatus}
import scala.collection.JavaConverters._


case class GOCDScalar(conf: FrameworkConfig, poller: GOCDPoller, driver: MesosSchedulerDriver) extends GocdMesosLogger {

  def reconcileTasks() = {
    logger.info("Going to reconcile tasks now")
    val runningTasks = runningTaskIds.map(id => TaskStatus
      .newBuilder()
      .setTaskId(TaskID
        .newBuilder()
        .setValue(id)
        .build())
      .setState(TaskState.TASK_RUNNING)
      .build())
    logger.info(s"There are ${runningTasks.size} tasks that need to reconciled")
    driver.reconcileTasks(runningTasks.asJavaCollection)
  }

  def scale() = {
    logger.info("SCALAR going to do autoscale operation")
    val supply = getSupply
    val demand = getDemand
    if(demand > supply) {
      logger.info(s"The demand: $demand is greater than supply: $supply. Now computing the agents needed according to min and max agents.")
      val needed = computeScaleup(supply, demand, conf.goMaxAgents, conf.goMinAgents)
      if(needed > 0) {
        logger.info(s"Adding $needed more agents to the fleet.")
        scaleUp(needed)
      }
    } else if (demand < supply) {
      logger.info(s"The demand: $demand is less than supply: $supply. Now computing the agents not needed according to min and max agents.")
      val notNeeded = computeScaledown(supply, demand, conf.goMaxAgents, conf.goMinAgents)
      if(notNeeded > 0) {
        logger.info(s"Removing $notNeeded agents from the fleet.")
        scaleDown(notNeeded)
      }
    }
  }

  def reconcileAndScale() = {
    reconcileTasks()
    scale()
  }

  private[mesos] def runningTaskIds = runningTasks.map(_.goAgentUuid)

  private[mesos] def runningTasks = TaskQueue.getRunningTasks

  private[mesos] def pendingTasks = TaskQueue.getPendingTasks


  private[mesos] def getTotalAgents = {
    poller.getAllAgents.size
  }

  private[mesos] def getSupply = {
    // Supply is number of 'idle GoAgents that are launched via Mesos' + 'GoAgents that are waiting to be launched'
    val running = runningTasks
      .toList
      .map(_.goAgentUuid)
      .intersect(poller.getIdleAgents.map(_.id)).size
    val pending = pendingTasks.size
    running + pending
  }

  private[mesos] def getDemand = {
    // Demand is number of 'jobs pending in Go Server'  + 'agents in building state'
    poller.getPendingJobsCount + poller.getBuildingAgents.size
  }

  private[mesos] def scaleDown(agentCount: Int) = {
    val idleAgents = poller.getIdleAgents
    idleAgents.take(agentCount).foreach(agent => {
      TaskQueue.getRunningTasks.toList.find(_.goAgentUuid == agent.id).foreach { task =>
        driver.killTask(TaskID.newBuilder().setValue(task.mesosTaskId).build())
      }
    })
  }

  private[mesos] def scaleUp(agentCount: Int) = {
    for(_ <- 0 to agentCount) {
      TaskQueue.add(GoTask(conf.goAgentDocker))
    }
  }

  private[mesos] def computeScaleup(supply: Int, demand: Int, goMaxAgents: Int, goMinAgents: Int): Int = {
    assert(demand > supply)
    if(supply > goMaxAgents) {
      return 0
    }
    val needed = demand - supply
    if(supply + needed > goMaxAgents) {
      goMaxAgents - supply
    } else {
      needed
    }
  }


  private[mesos] def computeScaledown(supply: Int, demand: Int, goMaxAgents: Int, goMinAgents: Int): Int = {
    assert(supply > demand)
    if(supply < goMinAgents) {
      return 0
    }
    val notNeeded = supply - demand
    if(supply - notNeeded < goMinAgents) {
      supply - goMinAgents
    } else if(supply - notNeeded > goMaxAgents) {
      supply - goMaxAgents
    } else {
      notNeeded
    }
  }

}
