/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.model

trait State {
    def elements(): Int

    def toSeq(): Seq[Double]

    def isEmpty(): Boolean
}

class EmptyState extends State {
    override def elements(): Int = 0

    override def toSeq(): Seq[Double] = Seq()

    override def isEmpty(): Boolean = true
}
