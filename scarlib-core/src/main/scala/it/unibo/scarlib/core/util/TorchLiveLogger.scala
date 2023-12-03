/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.util

import it.unibo.scarlib.core.neuralnetwork.TorchSupport
import me.shadaj.scalapy.py

object TorchLiveLogger extends Logger{
    private val writer = TorchSupport.logger().SummaryWriter()

    def logScalar(tag: String, value: Double, tick: Int): Unit = writer.add_scalar(tag, value, tick)

    def logAny(tag: String, value: py.Dynamic, tick: Int): Unit = writer.add_scalar(tag, value, tick)
}


