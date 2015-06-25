package com.indix.mesos

import java.util.concurrent.ConcurrentLinkedQueue


case class GoTask(cmdString: String, dockerImage: String, uri: String) {}

object TaskQueue {
  val queue : ConcurrentLinkedQueue[GoTask] = new ConcurrentLinkedQueue[GoTask]()

  def enqueue(task : GoTask) {
    queue.offer(task)
  }

  def dequeue : GoTask = {
    queue.poll()
  }
 }

