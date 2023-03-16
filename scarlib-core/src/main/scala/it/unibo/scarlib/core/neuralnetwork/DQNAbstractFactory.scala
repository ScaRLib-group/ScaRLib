package it.unibo.scarlib.core.neuralnetwork

abstract class DQNAbstractFactory[T] {
  def createNN(): T
}
