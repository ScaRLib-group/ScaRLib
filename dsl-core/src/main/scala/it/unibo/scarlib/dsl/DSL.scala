/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.dsl

import it.unibo.scarlib.core.model.{Action, Environment, LearningConfiguration, ReplayBuffer, RewardFunction, State}
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
import it.unibo.scarlib.core.system.{CTDEAgent, CTDESystem}
import scala.reflect.runtime.{universe => ru}

import scala.concurrent.ExecutionContext

object DSL {

  private var rf: Option[RewardFunction] = None
  private var env: Option[Environment] = None
  private var ds: Option[ReplayBuffer[State, Action]] = None
  private var lc: Option[LearningConfiguration] = None
  private var actionSpace: Seq[Action] = Seq.empty
  private var nAgents: Int = 0

  def CTDELearningSystem(init: => Unit)(implicit context: ExecutionContext, encoding: NeuralNetworkEncoding[State]): CTDESystem = {
    init
    var agentsSeq: Seq[CTDEAgent] = Seq.empty
    for (n <- 0  to nAgents) {
      agentsSeq = agentsSeq :+ new CTDEAgent(n, env.get, actionSpace, ds.get)
    }
    new CTDESystem(agentsSeq, env.get, ds.get, actionSpace, lc.get)
  }

  def rewardFunction(init: => RewardFunction): Unit = {
    rf = Option(init)
  }

  def environment(init: => String)(implicit config: Environment => Unit): Unit = {
    val name = init
    val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classSymbol = runtimeMirror.classSymbol(Class.forName(name))
    val classMirror = runtimeMirror.reflectClass(classSymbol)
    val constructor = classSymbol.typeSignature.members.filter(_.isConstructor).toList.head.asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor).apply(rf.get, actionSpace)
    val e = constructorMirror.asInstanceOf[Environment]
    config(e)
    env = Option(e)
  }

  def dataset(init: => ReplayBuffer[State, Action]): Unit = {
    ds = Option(init)
  }

  def learningConfiguration(init: => LearningConfiguration): Unit = {
    lc = Option(init)
  }

  def actionSpace(init: => Seq[Action]): Unit = {
    actionSpace = init
  }

  def agents(init: => Int): Unit = {
    nAgents = init
  }

}