package com.indix.mesos

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.{TaskState, TaskID, TaskStatus}
import scala.collection.JavaConverters._


case class GOCDScalar(conf: FrameworkConfig, poller: GOCDPoller, driver: MesosSchedulerDriver) {


  def runningTaskIds = TaskQueue.getAllJobIds


  def reconcileTasks() = {
    val runningTasks = runningTaskIds.map(id => TaskStatus
      .newBuilder()
      .setTaskId(TaskID
        .newBuilder()
        .setValue(id)
        .build())
      .setState(TaskState.TASK_RUNNING)
      .build())
    driver.reconcileTasks(runningTasks.asJavaCollection)
  }

  def scale() = {
    val supply = getSupply
    val demand = getDemand
    if(demand > supply) {
      val needed = computeScaleup(supply, demand, conf.goMaxAgents, conf.goMinAgents)
      scaleUp(needed)
    } else if (demand < supply) {
      val notNeeded = computeScaledown(supply, demand, conf.goMaxAgents, conf.goMinAgents)
      scaleDown(notNeeded)
    }
  }

  def reconcileAndScale() = {
    reconcileTasks()
    scale()
  }

  def getTotalAgents = {
    poller.getAllAgents.size
  }

  def getSupply = {
    // Supply is number of 'idle GoAgents that are launched via Mesos' + 'GoAgents that are waiting to be launched'
    TaskQueue
      .getRunningJobs
      .toList
      .map(_.goAgentUuid)
      .union(poller.getIdleAgents.map(_.id)).size
    + TaskQueue.getPendingJobs.size
  }

  def getDemand = {
    // Demand is number of 'jobs pending in Go Server'  + 'agents in building state'
    poller.getPendingJobsCount + poller.getBuildingAgents.size
  }

  def scaleDown(agentCount: Int) = {
    val idleAgents = poller.getIdleAgents
    idleAgents.take(agentCount).foreach(agent => {
      TaskQueue.getRunningJobs.toList.find(_.goAgentUuid == agent.id).foreach { task =>
        driver.killTask(TaskID.newBuilder().setValue(task.mesosTaskId).build())
      }
    })
  }

  def scaleUp(agentCount: Int) = {
    for(_ <- 0 to agentCount) {
      TaskQueue.add(GoTask(conf.goAgentDocker))
    }
  }

  def computeScaleup(supply: Int, demand: Int, goMaxAgents: Int, goMinAgents: Int): Int = {
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


  def computeScaledown(supply: Int, demand: Int, goMaxAgents: Int, goMinAgents: Int): Int = {
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
