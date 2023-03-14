package it.unibo.scarlib.core.model

import scala.concurrent.Future

trait Agent {
  def step(): Future[Unit]
}
