package it.unibo.scarlib.core.model

import scala.concurrent.Future

abstract class GeneralEnvironment(rewardFunction: RewardFunction, actionSpace: Seq[Action]) {
  def step(action: Action, agentId: Int): Future[(Double, State)]

  def observe(agentId: Int): State

  def reset(): Unit

  def log(): Unit

  def logOnFile(): Unit
}
