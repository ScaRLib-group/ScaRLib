/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

@RunWith(classOf[JUnitRunner])
class DecayTest extends AnyFlatSpec with Matchers:

  "Decay values" should "decrease in time" in {
    val d = new ExponentialDecay(1.0, 0.1, 0.01)
    var values = List.empty[Double]
    for (i <- 1 to 10) {
      d.update()
      values = values :+ d.value()
    }
    values shouldBe values.sortWith( _ >= _)
  }

  "Values" should "be bounded" in {
    val bound = 0.5
    val d = new ExponentialDecay(1.0, 0.1, bound)
    var values = List.empty[Double]
    for (i <- 1 to 20) {
      d.update()
      values = values :+ d.value()
    }
    values.takeRight(10).forall { _ == bound} shouldBe true
  }

