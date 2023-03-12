package it.unibo.scarlib.core.model

case class LearningConfiguration[T](
    epsilon: Decay[T],
    gamma: Double,
    learningRate: Double,
    batchSize: Int,
    updateEach: Int
    )
