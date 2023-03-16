package it.unibo.scarlib.core.neuralnetwork

abstract class DQNAbstractFactory {
  def createNN[T](): T
}
