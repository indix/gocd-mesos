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

  def getTotalAgents = {
    poller.getGoAgents.size
  }

  def getSupply = {
    TaskQueue
      .getRunningJobs
      .toList
      .map(_.goAgentUuid)
      .union(poller.goIdleAgents.map(_.id)).size
    + TaskQueue.getPendingJobs.size
  }

  def getDemand = {
    poller.getPendingJobsCount + poller.getBuildingAgents.size
  }

  def scaleDown(agentCount: Int) = {
    val idleAgents = poller.goIdleAgents
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
    val needed = demand - supply
    if(supply + needed > goMaxAgents) {
      goMaxAgents - supply
    } else {
      needed
    }
  }


  def computeScaledown(supply: Int, demand: Int, goMaxAgents: Int, goMinAgents: Int): Int = {
    assert(supply > demand)
    val notNeeded = supply - demand
    if(supply - notNeeded < goMinAgents) {
      supply - goMinAgents
    } else if(supply - notNeeded > goMaxAgents) {
      supply - goMaxAgents
    } else {
      notNeeded
    }
  }


  def scale = {
    val supply = getSupply
    val demand = getDemand
    if(demand > supply) {
      computeScaleup(supply, demand, conf.goMaxAgents, conf.goMinAgents)
    } else if (demand < supply) {
      computeScaledown(supply, demand, conf.goMaxAgents, conf.goMinAgents)
    } else {
      // do Nothing
    }
  }
}
