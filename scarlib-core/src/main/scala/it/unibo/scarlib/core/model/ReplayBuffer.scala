/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

import scala.util.Random
import collection.mutable.ArrayDeque

/** The experience gained by an agent from an interaction with the environment
 *
 * @param actualState the state in which the environment is
 * @param action the action performed by the agent
 * @param reward the reward earned by the agent after taking the action from the actual state
 * @param nextState the state in which the environment goes into after the action taken by the agent
 */
case class Experience[State, Action](actualState: State, action: Action, reward: Double, nextState: State)

/** The container of agents experience */
trait ReplayBuffer[State, Action]{

  /** Inserts new experience */
  def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit

  /** Empty the buffer */
  def reset(): Unit

  /** Gets a sub-sample of the experience stored by the agents */
  def subsample(batchSize: Int): Seq[Experience[State, Action]]

  /** Gets all the experience stored by the agents */
  def getAll(): Seq[Experience[State, Action]]

  /** Gets the buffer size */
  def size(): Int

}

object ReplayBuffer{
  def apply[S, A](size: Int): ReplayBuffer[S, A] = {
    new BoundedQueue[S, A](size, 42)
  }

  private class BoundedQueue[State, Action](bufferSize: Int, seed: Int) extends ReplayBuffer[State, Action]{

    private var queue: ArrayDeque[Experience[State, Action]] = ArrayDeque.empty

    override def reset(): Unit = queue = ArrayDeque.empty[Experience[State, Action]]

    override def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit =
      queue = (queue :+ Experience(actualState, action, reward, nextState)).takeRight(bufferSize)

    override def subsample(batchSize: Int): Seq[Experience[State, Action]] =
      new Random(seed).shuffle(queue).take(batchSize).toSeq

    override def getAll(): Seq[Experience[State, Action]] = queue.toSeq

    override def size(): Int = queue.size
  }

}
