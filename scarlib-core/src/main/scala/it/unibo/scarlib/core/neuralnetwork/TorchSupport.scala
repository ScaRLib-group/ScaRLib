/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.neuralnetwork

import me.shadaj.scalapy.py

object TorchSupport extends DeepLearningSupport[py.Module] {

  override def deepLearningLib(): py.Module = py.module("torch")

  override def neuralNetworkModule(): py.Module = py.module("torch.nn")

  override def optimizerModule(): py.Module = py.module("torch.optim")

  override def logger(): py.Module = py.module("torch.utils.tensorboard")

  override def arrayModule: py.Module = py.module("numpy")
}
