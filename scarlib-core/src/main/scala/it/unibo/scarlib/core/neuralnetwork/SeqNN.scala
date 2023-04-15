/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.neuralnetwork

import it.unibo.scarlib.core.neuralnetwork.TorchSupport.{neuralNetworkModule => nn}
import me.shadaj.scalapy.py

object SeqNN extends DQN {
  def apply(input: Int, hidden: Int, output: Int): py.Dynamic =
    nn().Sequential(
      nn().Linear(input, hidden).cuda(),
      nn().ReLU().cuda(),
      nn().Linear(hidden, hidden).cuda(),
      nn().ReLU().cuda(),
      nn().Linear(hidden, hidden).cuda(),
      nn().ReLU().cuda(),
      nn().Linear(hidden, hidden).cuda(),
      nn().ReLU().cuda(),
      nn().Linear(hidden, output).cuda()
    ).cuda()
}
