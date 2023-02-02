package it.unibo.scarlib.core.neuralnetwork

import me.shadaj.scalapy.py

object TorchSupport extends DeepLearningSupport[py.Module]:
  override def deepLearningLib: py.Module = py.module("torch")
  override def neuralNetworkModule: py.Module = py.module("torch.nn")
  override def optimizerModule: py.Module = py.module("torch.optim")
  override def logger: py.Module = py.module("torch.utils.tensorboard")