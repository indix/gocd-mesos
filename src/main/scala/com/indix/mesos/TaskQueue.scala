package com.indix.mesos

import java.util.concurrent.ConcurrentLinkedQueue


case class GoTaskResource(cpu: Double = 1, memory: Double = 256, disk: Double = 1*1024)
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

