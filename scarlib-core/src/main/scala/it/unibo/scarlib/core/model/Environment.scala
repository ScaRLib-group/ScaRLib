/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

import scala.concurrent.Future

/** The environment the agents interact with
 *
 * @param rewardFunction the function that evaluates the action performed by an agent
 * @param actionSpace all the possible actions an agent can perform
 */
abstract class Environment(rewardFunction: RewardFunction, actionSpace: Seq[Action]) {

  /** A single interaction with an agent
   *
   * @param action the action performed by the agent
   * @param agentId the agent unique id
   * @return a [[Future]] that contains the reward of the action and the next state
   */
  def step(action: Action, agentId: Int): Future[(Double, State)]

  /** Gets the current state of the environment */
  def observe(agentId: Int): State

  /** Resets the environment to the initial state */
  def reset(): Unit

  def log(): Unit

  def logOnFile(): Unit
}
