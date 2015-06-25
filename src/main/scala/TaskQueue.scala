package com.indix.mesos

import java.util.concurrent.ConcurrentLinkedQueue


case class GoTaskResource(cpu: Double = 1, memory: Double = 512, disk: Double = 0)
case class GoTask(cmdString: String, dockerImage: String, uri: String, resource: GoTaskResource = GoTaskResource()) {}

object TaskQueue {
  val queue : ConcurrentLinkedQueue[GoTask] = new ConcurrentLinkedQueue[GoTask]()

  def enqueue(task : GoTask) {
    queue.offer(task)
  }

  def dequeue : GoTask = {
    queue.poll()
  }
 }

