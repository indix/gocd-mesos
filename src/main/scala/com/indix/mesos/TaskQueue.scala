package com.indix.mesos

import java.util.UUID

import org.apache.mesos.Protos.TaskState


object GoTaskState {
  abstract class GoTaskState
  object Pending extends GoTaskState
  object Scheduled extends GoTaskState
  object Running extends GoTaskState
  object Stopped extends GoTaskState
}

case class GoTaskResource(cpu: Double = 1, memory: Double = 256, disk: Double = 1*1024)


case class GoTask(dockerImage: String,
                  uri: String,
                  state: GoTaskState.GoTaskState,
                  goAgentUuid: String,
                  mesosTaskId: String,
                  mesosFailureMessage: Option[String] = None,
                  resource: GoTaskResource = GoTaskResource()) {
  def updateState(state: TaskState): GoTask = {
    state match {
      case TaskState.TASK_FAILED | TaskState.TASK_KILLED | TaskState.TASK_FINISHED => this.copy(state = GoTaskState.Stopped, mesosFailureMessage = Some(""))
      case _ => this
    }
  }
}

object GoTask {
  def generateTaskId(): String = {
    "Mesos-gocd-task-" + System.currentTimeMillis()
  }

  def generateAgentUUID(): String = {
    UUID.randomUUID().toString
  }

  def apply(dockerImage: String) = {
    new GoTask(dockerImage, "", GoTaskState.Pending, generateAgentUUID(), generateTaskId(), None)
  }
}

case class TaskQueue(queue: Map[String, GoTask] = Map.empty[String, GoTask]) {

  def getRunningJobs = {
    queue.values.filter { task: GoTask =>
      task.state match {
        case GoTaskState.Running => true
        case _ => false
      }
    }
  }

  def getPendingJobs = {
    queue.values.filter { task: GoTask =>
      task.state match {
        case GoTaskState.Pending => true
        case GoTaskState.Scheduled=> true
        case _ => false
      }
    }
  }


  def add(task: GoTask): TaskQueue = {
    this.copy(queue = queue + (task.mesosTaskId -> task))
  }

  def findNext: Option[GoTask] = {
    queue.values.find(task => {
      task.state match {
        case GoTaskState.Pending => true
        case _ => false
      }
    })
  }

  def updateState(id: String, state: TaskState): TaskQueue = {
    this.queue.get(id).map(task => {
      this.copy(queue = queue + (id -> task.updateState(state)))
    }).getOrElse(this)
  }

  def prune: TaskQueue = {
    val updatedQueue = this.queue.filterNot(pair => {
      val (id, task) = pair
      task.state match {
        case GoTaskState.Stopped => true
        case _ => false
      }
    })
    this.copy(queue = updatedQueue)
  }
}

object TaskQueue {

  var queue = TaskQueue()


  def getAllJobIds = queue.queue.keys.toList

  def getRunningJobs = queue.getRunningJobs

  def getPendingJobs = queue.getPendingJobs


  def add(task : GoTask) {
    synchronized {
      queue = queue.add(task)
    }
  }

  def findNext = {
    queue.findNext
  }

  def updateState(id: String, state: TaskState) = {
    synchronized {
      queue = queue.updateState(id, state)
    }
  }

  def reset = {
    queue.prune
  }
 }

