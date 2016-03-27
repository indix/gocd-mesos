package com.indix.mesos.common

import java.util.logging.Logger

trait GocdMesosLogger {
  val logger = Logger.getLogger(this.getClass.getName)
}
