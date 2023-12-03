/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scafi

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scarlib.core.model.State

/** The scafi logic used to construct the state with aggregate programming */
abstract class ScafiProgram
  extends AggregateProgram
  with StandardSensors
  with ScafiAlchemistSupport
  {

  override def main(): Unit = {
    makeActions()
    node.put("state", computeState())
  }

  protected def computeState(): State

  protected def makeActions(): Unit

}
