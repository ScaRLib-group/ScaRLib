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
      nn().Linear(hidden, output).cuda()
    ).cuda()
}
