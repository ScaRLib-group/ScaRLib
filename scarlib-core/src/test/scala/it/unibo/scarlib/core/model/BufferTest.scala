/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

@RunWith(classOf[JUnitRunner])
class BufferTest extends AnyFlatSpec with Matchers:
  "Insert method" should "increase buffer size when it is not full" in {
    val buffer = ReplayBuffer[Double, Int](10)
    buffer.insert(1.0, 1, 10, 1.3)
    buffer.getAll.size shouldBe 1
  }

  "Insert method" should "not overflow buffer size" in {
    val buffer = ReplayBuffer[Double, Int](5)
    buffer.insert(1.0, 1, 10, 1.3)
    buffer.insert(1.1, 1, 10, 1.3)
    buffer.insert(1.2, 1, 10, 1.3)
    buffer.insert(1.3, 1, 10, 1.3)
    buffer.insert(1.4, 1, 10, 1.3)
    buffer.insert(1.5, 1, 10, 1.3)
    buffer.getAll.size shouldBe 5
  }

