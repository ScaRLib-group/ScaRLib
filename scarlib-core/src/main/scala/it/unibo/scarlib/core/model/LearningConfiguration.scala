package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.DQNAbstractFactory

case class LearningConfiguration(
    epsilon: Decay[Double] = new ExponentialDecay(0.9, 0.1, 0.01),
    gamma: Double = 0.9,
    learningRate: Double = 0.0005,
    batchSize: Int = 32,
    updateEach: Int = 100,
    dqnFactory: DQNAbstractFactory
    )
