/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.DQNAbstractFactory

import scala.util.Random

/** The wrapper of all the hyper-parameters that the user can set
 * @param epsilon the decay used to solve the exploitation-exploration problem
 * @param gamma the discount factor
 * @param learningRate the learning rate used to train the neural network
 * @param batchSize the size of the batch of agents experience used to update the neural network
 * @param updateEach represents every how many iterations the actual and the target neural networks are synchronized
 * @param random
 * @param dqnFactory the factory of the neural network used to approximate the policy
 * @param snapshotPath the path to save the policy snapshots
 */
case class LearningConfiguration(
    epsilon: Decay[Double] = new ExponentialDecay(0.9, 0.1, 0.01),
    gamma: Double = 0.9,
    learningRate: Double = 0.0005,
    batchSize: Int = 32,
    updateEach: Int = 100,
    random: Random = new Random(1),
    dqnFactory: DQNAbstractFactory[_],
    snapshotPath: String
    )
