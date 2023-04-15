/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

trait Decay[T]{
  def update(): Unit
  def value(): T
}


class ExponentialDecay(initialValue: Double, rate: Double, bound: Double) extends Decay[Double]{

  private var elapsedTime: Int = 0
  override def update(): Unit = elapsedTime = elapsedTime + 1
  override def value(): Double = {
    val v = initialValue * Math.pow(1 - rate, elapsedTime)
    if (v > bound) { v } else { bound }
  }
}

class ConstantDecay(initialValue: Double) extends Decay[Double]{
  override def update(): Unit = ()
  override def value(): Double = initialValue
}
