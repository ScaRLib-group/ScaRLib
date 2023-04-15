/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.DQNAbstractFactory

import scala.util.Random

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
