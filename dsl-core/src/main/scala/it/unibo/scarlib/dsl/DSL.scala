/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.dsl
import scala.reflect.runtime.universe as ru

import it.unibo.scarlib.core.deepRL.{CTDESystem, IndependentAgent}
import it.unibo.scarlib.core.model.*

import scala.collection.mutable
import scala.collection.mutable.Seq as MSeq
import scala.concurrent.ExecutionContext


object DSL {

    private var rf: Option[RewardFunction] = Option.empty
    private var env: Option[Environment] = Option.empty
    private var ds: Option[ReplayBuffer[State, Action]] = Option.empty
    private var actionSpace: Seq[Action] = Seq.empty
    private var lc: Option[LearningConfiguration] = Option.empty
    private var nAgents: Int = 0

    def CTDELearningSystem(init: Unit ?=> Unit)(using context: ExecutionContext): CTDESystem =
        given unit: Unit = ()

        init
        var agentsSeq: Seq[IndependentAgent] = Seq.empty
        for (n <- 0 to nAgents) {
            agentsSeq = agentsSeq :+ new IndependentAgent(n, env.get, actionSpace, ds.get)
        }
        new CTDESystem(agentsSeq, env.get, ds.get, actionSpace, lc.get)

    def environment(init: Unit ?=> String) =
        given unit: Unit = ()

        val name: String = init
        val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)
        val classSymbol = runtimeMirror.classSymbol(Class.forName(name))
        val classMirror = runtimeMirror.reflectClass(classSymbol)
        val constructor = classSymbol.typeSignature.members.filter(_.isConstructor).toList.head.asMethod
        val constructorMirror = classMirror.reflectConstructor(constructor).apply(rf.get, actionSpace)
        env = Option(constructorMirror.asInstanceOf[Environment])

    def rewardFunction(init: Unit ?=> RewardFunction) =
        given unit: Unit = ()

        rf = Option(init)

    def actions(init: Unit ?=> Seq[Action]) =
        given unit: Unit = ()

        actionSpace = init

    def dataset(init: Unit ?=> ReplayBuffer[State, Action]) =
        given unit: Unit = ()

        ds = Option(init)

    def agents(init: Unit ?=> Int) =
        given unit: Unit = ()

        nAgents = init

    def learningConfiguration(init: Unit ?=> LearningConfiguration) =
        given unit: Unit = ()
        lc = Option(init)

}

