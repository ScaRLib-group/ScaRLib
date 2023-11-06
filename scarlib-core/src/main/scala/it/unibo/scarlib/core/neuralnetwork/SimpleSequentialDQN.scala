/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.neuralnetwork

import it.unibo.scarlib.core.model.AutodiffDevice
import it.unibo.scarlib.core.neuralnetwork.TorchSupport.{neuralNetworkModule => nn}
import me.shadaj.scalapy.py

/** A simple feed-forward neural network */
object SimpleSequentialDQN extends DQN {
  def apply(input: Int, hidden: Int, output: Int): py.Dynamic =
    nn()
      .Sequential(
        nn().Linear(input, hidden).to(AutodiffDevice()),
        nn().ReLU().to(AutodiffDevice()),
        nn().Linear(hidden, hidden).to(AutodiffDevice()),
        nn().ReLU().to(AutodiffDevice()),
        nn().Linear(hidden, output).to(AutodiffDevice())
      )
      .to(AutodiffDevice())
}
