package it.unibo.scarlib.core.neuralnetwork

import it.unibo.scarlib.core.model.AutodiffDevice
import it.unibo.scarlib.core.neuralnetwork.TorchSupport.{neuralNetworkModule => nn}
import me.shadaj.scalapy.py

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
