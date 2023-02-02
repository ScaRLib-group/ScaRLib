package it.unibo.scarlib.core.neuralnetwork

import me.shadaj.scalapy.py
import it.unibo.scarlib.core.neuralnetwork.TorchSupport.{neuralNetworkModule => nn}

object SimpleSequentialDQN extends DQN:
  def apply(input: Int, hidden: Int, output: Int): py.Dynamic =
    nn.Sequential(
      nn.Linear(input, hidden),
      nn.ReLU(),
      nn.Linear(hidden, hidden),
      nn.ReLU(),
      nn.Linear(hidden, output)
    )