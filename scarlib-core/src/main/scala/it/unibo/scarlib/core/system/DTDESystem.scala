/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.system

import scala.annotation.tailrec
import it.unibo.scarlib.core.model.{Environment, State}
import it.unibo.scarlib.core.neuralnetwork.{NeuralNetworkEncoding, NeuralNetworkSnapshot}

import scala.concurrent.ExecutionContext
import scala.concurrent.{Await, Future}

/** A system in which agents work in a Decentralized Training Decentralized Execution way
 *
 * @param agents all the agents
 * @param environment the environment in which the agents interact
 */

class DTDESystem(
                  agents: Seq[DTDEAgent],
                  environment: Environment
)(implicit context: ExecutionContext, encoding: NeuralNetworkEncoding[State]){

  /** Starts the learning process
   *
   * @param episodes the number of episodes agents are trained for
   * @param episodeLength the length of each episode
   */
  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit = {
      if (time > 0) {
        agents.foreach(_.step())
        environment.log()
        singleEpisode(time - 1)
      }
    }

    if (episodes > 0) {
      println("Episode: " + episodes)
      environment.reset()
      singleEpisode(episodeLength)
      agents.foreach(_.snapshot(episodes))
      learn(episodes - 1, episodeLength)
    }
  }


  /** Starts the testing process
   *
   * @param episodeLength the length of the episode
   * @param policy the snapshot of the policy to be used
   */
  final def runTest(episodeLength: Int, policy: NeuralNetworkSnapshot): Unit = {
    agents.foreach(_.setTestPolicy(policy))
    environment.reset()
    episode(episodeLength)

    @tailrec
    def episode(time: Int): Unit = {
      agents.foreach(_.step())
      episode(time - 1)
    }
  }
}