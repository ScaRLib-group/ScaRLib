package it.unibo.scarlib.core.neuralnetwork

trait DeepLearningSupport[M]{
  def deepLearningLib: M

  def neuralNetworkModule: M

  def optimizerModule: M

  def logger: M

}

