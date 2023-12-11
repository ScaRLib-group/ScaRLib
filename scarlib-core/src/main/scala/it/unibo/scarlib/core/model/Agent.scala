/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

import scala.concurrent.Future

/** A generic agent */
trait Agent {
  /** A single interaction of the agent with the environment
   * @return a [[Future]] that indicates when the interaction is completed
   * */
  def step(): Future[Unit]
}
