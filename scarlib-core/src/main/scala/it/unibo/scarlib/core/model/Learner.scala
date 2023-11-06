/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

trait Learner {

  /** Gets the optimal policy */
  val optimal: State => Action

  /** Gets the behavioural policy */
  val behavioural: State => Action

  /** Records the experience gained from the last agent-environment interaction */
  def record(state: State, action: Action, reward: Double, nextState: State): Unit

  /** Improves the policy following the learning algorithm */
  def improve(): Unit

  /** Takes a snapshot of the current policy */
  def snapshot(episode: Int, agentId: Int): Unit

}
