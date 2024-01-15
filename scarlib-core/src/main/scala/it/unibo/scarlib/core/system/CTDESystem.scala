/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.system

import it.unibo.scarlib.core.model.{Action, Decay, DeepQLearner, Environment, LearningConfiguration, ReplayBuffer, State}
import it.unibo.scarlib.core.util.{Logger, TorchLiveLogger}
import it.unibo.scarlib.core.neuralnetwork.{NeuralNetworkEncoding, NeuralNetworkSnapshot}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.{Await, Future}

/** A system in which agents work in a Centralized Training Decentralized Execution way
 *
 * @param agents all the agents
 * @param environment the environment in which the agents interact
 * @param dataset the global container of agents experience
 * @param actionSpace all the possible actions an agent can perform
 * @param learningConfiguration all the hyper-parameters specified by the user
 * @param context the [[ExecutionContext]], it is used to configure how and on which thread pools asynchronous tasks (such as Futures) will run
 */
class CTDESystem(
                  agents: Seq[CTDEAgent],
                  environment: Environment,
                  dataset: ReplayBuffer[State, Action],
                  actionSpace: Seq[Action],
                  learningConfiguration: LearningConfiguration,
                  logger: Logger = TorchLiveLogger
)(implicit context: ExecutionContext, encoding: NeuralNetworkEncoding[State]) {

  private val epsilon: Decay[Double] = learningConfiguration.epsilon

  private val learner = new DeepQLearner(dataset, actionSpace, learningConfiguration, logger)




  /** Starts the learning process
   *
   * @param episodes the number of episodes agents are trained for
   * @param episodeLength the length of each episode
   */
  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit =
      if (time > 0) {
        println("Time: " + time)
        agents.foreach(_.notifyNewPolicy(learner.behavioural))
        Await.ready(Future.sequence(agents.map(_.step())), scala.concurrent.duration.Duration.Inf)
        environment.log()
        learner.improve()
        singleEpisode(time - 1)
      }

    if (episodes > 0) {
      println("Episode: " + episodes)
      environment.reset()
      singleEpisode(episodeLength)
      epsilon.update()
      learner.snapshot(episodes, 0)
      learn(episodes - 1, episodeLength)
    }

  }

    final def learn(episodes: Int, episodeLength: Int, snapshot: String): Unit = {
        learner.loadSnapshot(snapshot)
        learn(episodes, episodeLength)
    }

  /** Starts the testing process
   *
   * @param episodeLength the length of the episode
   * @param policy the snapshot of the policy to be used
   */
  final def runTest(episodeLength: Int, policy: NeuralNetworkSnapshot): Unit = {
    val p: State => Action =
      DeepQLearner.policyFromNetworkSnapshot(policy.path, encoding, policy.inputSize, policy.hiddenSize, actionSpace)
    agents.foreach(_.notifyNewPolicy(p))
    environment.reset()
    episode(episodeLength)

    @tailrec
    def episode(time: Int): Unit = {
      println(time)
      if (time > 0) {
        Await.ready(Future.sequence(agents.map(_.step())), scala.concurrent.duration.Duration.Inf)
        episode(time - 1)
      }
    }
  }

}
