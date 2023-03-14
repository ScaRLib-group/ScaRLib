package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.TorchSupport._

object AutodiffDevice {

  def apply() =
    deepLearningLib()
      .device(if (deepLearningLib().cuda.is_available().as[Boolean]) "cuda" else "cpu")
}
