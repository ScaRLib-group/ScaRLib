/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.TorchSupport._


/** Sets whether a GPU is available for training or if a cpu must be used */
object AutodiffDevice {

  def apply() =
    deepLearningLib()
      .device(if (deepLearningLib().cuda.is_available().as[Boolean]) "cuda" else "cpu")
//        .device("cpu")
}
