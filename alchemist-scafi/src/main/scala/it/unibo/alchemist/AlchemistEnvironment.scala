/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.alchemist

import it.unibo.alchemist.boundary.swingui.impl.SingleRunGUI
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.{Simulation, Status}
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.scarlib.core.model._
import it.unibo.scarlib.core.util.{AgentGlobalStore, TorchLiveLogger}
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.swing.WindowConstants
import _root_.scala.concurrent.{Future, Promise}
import _root_.scala.util.Success

/** An environment that uses the Alchemist simulator */
class AlchemistEnvironment(
    rewardFunction: RewardFunction,
    actionSpace: Seq[Action],
    envDefinition: String,
    outputStrategy: OutputStrategy = NoOutput,
    randomSeed: Option[Int] = None
) extends Environment(rewardFunction, actionSpace) {

  private def dt = 1.0
  private val file = new File(envDefinition)
  private val alchemistUtil = new AlchemistUtil()
  private var engine: Engine[Any, Nothing] = _
  this.reset()
  private var agentPromises = Map.empty[Int, Promise[(Double, State)]]
  private var oldState = Map.empty[Int, State]
  private var ticks = 0

  override def step(action: Action, agentId: Int): Future[(Double, State)] = {
    agentPromises = agentPromises + (agentId -> Promise[(Double, State)]())
    val actualState = observe(agentId)
    oldState = oldState + (agentId -> actualState)
    val node = engine.getEnvironment.getNodeByID(agentId)
    node.setConcentration(new SimpleMolecule("action"), action)
    val result =
      agentPromises(agentId).future
    if (agentPromises.size == engine.getEnvironment.getNodeCount) {
      alchemistUtil.incrementTime(dt, engine)
      for ((id, promise) <- agentPromises) {
        val newState = observe(id)
        val r = rewardFunction.compute(oldState(id), action, newState)
        promise.complete(Success(r, newState))
      }
      agentPromises = Map.empty
      ticks += 1
    }
    result
  }

  override def observe(agentId: Int): State = {
    val state = engine.getEnvironment.getNodeByID(agentId).getConcentration(new SimpleMolecule("state"))
    if (state == null) {
      new EmptyState()
    } else {
      state.asInstanceOf[State]
    }
  }

  override def reset(): Unit = {
    if (engine != null) {
      engine.terminate()
      engine.waitFor(Status.TERMINATED, Long.MaxValue, TimeUnit.SECONDS)
    }
    engine = alchemistUtil.load(file, randomSeed)
    outputStrategy.output(engine)
  }

  def currentNodeCount: Int = engine.getEnvironment.getNodeCount

  override def log(): Unit = {
    AgentGlobalStore.sumAllNumeric(AgentGlobalStore()).foreach { case (k, v) =>
      TorchLiveLogger.logScalar(k, v, ticks)
    }
    AgentGlobalStore().clearAll()
  }

}

sealed trait OutputStrategy {
  def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit

  protected def render[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    val windows = java.awt.Window.getWindows
    windows.foreach(_.dispose())
    SingleRunGUI.make[T, P](simulation, WindowConstants.DO_NOTHING_ON_CLOSE)
  }
}

object NoOutput extends OutputStrategy {
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {}
}

class ShowEach(each: Int) extends OutputStrategy {
  private var episodes = 0
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    if (episodes % each == 0) {
      // get current awt window and close it
      render(simulation)
    }
    episodes += 1
  }
}

class After(ticks: Int) extends OutputStrategy {
  private var episodes = 0
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    println(s"episodes: $episodes")
    if (ticks == episodes) {
      render(simulation)
    }
    episodes += 1
  }
}