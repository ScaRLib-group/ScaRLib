/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.system

import it.unibo.scarlib.core.model._
import scala.reflect.io.File
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** An agent that works in a [[CTDESystem]]
 *
 * @param agentId the unique id of the agent
 * @param environment the environment in which the agents interact
 * @param actionSpace all the possible actions an agent can perform
 * @param dataset the dataset that will contain the agents experience
 */
class CTDEAgent(
                        agentId: Int,
                        environment: Environment,
                        actionSpace: Seq[Action],
                        dataset: ReplayBuffer[State, Action],
) extends Agent {
  private var policy: State => Action = _

  /** A single interaction of the agent with the environment */
  override def step(): Future[Unit] = {
    val state = environment.observe(agentId)
    if (!state.isEmpty()) {
      val action = policy(state)
      environment
        .step(action, agentId)
        .map { result =>
          dataset.insert(Experience(state, action, result._1, result._2))
        }
        .map(_ => ())

    } else {
      environment.step(Random.shuffle(actionSpace).head, agentId)
      Future {}
    }
  }

  /** Sets a new policy */
  def notifyNewPolicy(newPolicy: State => Action): Unit =
    policy = newPolicy

}