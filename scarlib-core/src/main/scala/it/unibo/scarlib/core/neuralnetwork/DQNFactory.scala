package it.unibo.scarlib.core.neuralnetwork

abstract class DQNFactory {
  def createNN(): DQN
}
