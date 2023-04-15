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

case class Experience[State, Action](actualState: State, action: Action, reward: Double, nextState: State)

trait ReplayBuffer[State, Action]{
  def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit

  def reset(): Unit

  def subsample(batchSize: Int): Seq[Experience[State, Action]]

  def getAll(): Seq[Experience[State, Action]]

  def size(): Int
}

object ReplayBuffer{
  def apply[S, A](size: Int): ReplayBuffer[S, A] = {
    new BoundedQueue[S, A](size)
  }

  private class BoundedQueue[State, Action](bufferSize: Int) extends ReplayBuffer[State, Action]{

    private var queue: Seq[Experience[State, Action]] = Seq.empty

    override def reset(): Unit = Seq.empty

    override def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit =
      queue = (Experience(actualState, action, reward, nextState) +: queue).take(bufferSize)

    override def subsample(batchSize: Int): Seq[Experience[State, Action]] =
      new Random(42).shuffle(queue).take(batchSize)

    override def getAll(): Seq[Experience[State, Action]] = queue

    override def size(): Int = queue.size
  }

}
