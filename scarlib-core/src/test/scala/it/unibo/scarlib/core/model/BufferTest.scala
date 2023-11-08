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

class MyState(id: Int) extends State {
  override def isEmpty(): Boolean = false
}

class MyAction(id: Int) extends Action


@RunWith(classOf[JUnitRunner])
class BufferTest extends AnyFlatSpec with Matchers:
  "Insert method" should "increase buffer size when it is not full" in {
    val buffer = ReplayBuffer[MyState, MyAction](10)
    buffer.insert(new MyState(1), new MyAction(1), 10, new MyState(1))
    buffer.getAll.size shouldBe 1
  }

  "Insert method" should "not overflow buffer size" in {
    val buffer = ReplayBuffer[MyState, MyAction](5)
    buffer.insert(new MyState(1), new MyAction(1), 10, new MyState(1))
    buffer.insert(new MyState(2), new MyAction(2), 10, new MyState(2))
    buffer.insert(new MyState(3), new MyAction(3), 10, new MyState(3))
    buffer.insert(new MyState(4), new MyAction(4), 10, new MyState(4))
    buffer.insert(new MyState(5), new MyAction(5), 10, new MyState(5))
    buffer.getAll.size shouldBe 5
  }

