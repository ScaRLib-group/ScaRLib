/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.model

/** A generic state in which the environment can be */
trait State {

    /** Gets the number of elements in the state */
    def elements(): Int

    /** Converts the state into a format usable by the neural network */
    def toSeq(): Seq[Double]

    /** Checks if the state is empty */
    def isEmpty(): Boolean
}

/** An empty state */
class EmptyState extends State {
    override def elements(): Int = 0

    override def toSeq(): Seq[Double] = Seq()

    override def isEmpty(): Boolean = true
}
