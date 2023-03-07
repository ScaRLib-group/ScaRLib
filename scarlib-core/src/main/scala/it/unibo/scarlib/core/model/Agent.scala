package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.DQN
import me.shadaj.scalapy.py

trait Agent {
  def mode(): AgentMode
  def updatePolicy(newPolicy: py.Dynamic): Unit
  def act(): Action
}
