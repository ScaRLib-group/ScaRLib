/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.system

import it.unibo.scarlib.core.model.{Action, Agent, AgentMode, Decay, DeepQLearner, Environment, Experience, LearningConfiguration, ReplayBuffer, State}
import it.unibo.scarlib.core.neuralnetwork.{NeuralNetworkEncoding, NeuralNetworkSnapshot}
import it.unibo.scarlib.core.util.{Logger, TorchLiveLogger}

import scala.reflect.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** An agent that works in a [[DTDESystem]]
 *
 * @param agentId the unique id of the agent
 * @param environment the environment in which the agents interact
 * @param actionSpace all the possible actions an agent can perform
 * @param datasetSize the size of the dataset that will contain the agent experience
 * @param agentMode whether the agent is in training or testing
 * @param learningConfiguration all the hyper-parameters specified by the user
 */
class DTDEAgent(
                          agentId: Int,
                          environment: Environment,
                          actionSpace: Seq[Action],
                          datasetSize: Int,
                          agentMode: AgentMode = AgentMode.Training,
                          learningConfiguration: LearningConfiguration,
                          logger: Logger = TorchLiveLogger
)(implicit encoding: NeuralNetworkEncoding[State]) extends Agent {

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val epsilon: Decay[Double] = learningConfiguration.epsilon
  private val learner = new DeepQLearner(dataset, actionSpace, learningConfiguration, logger)
  private var testPolicy: State => Action = _

  /** A single interaction of the agent with the environment */
  override def step(): Future[Unit] = {
    val state = environment.observe(agentId)
    val policy = getPolicy
    val action = policy(state)
    environment
      .step(action, agentId)
      .map { result =>
        agentMode match {
          case AgentMode.Training =>
            dataset.insert(Experience(state, action, result._1, result._2))
            learner.improve()
            epsilon.update()
          case AgentMode.Testing => //do nothing
        }
      }
  }

  /** Makes a snapshot of the current policy */
  def snapshot(episode: Int): Unit = learner.snapshot(episode, agentId)

  /** Sets a new policy for testing */
  def setTestPolicy(p: NeuralNetworkSnapshot): Unit =
    testPolicy = DeepQLearner.policyFromNetworkSnapshot(p.path + s"-$agentId", encoding, p.inputSize, p.hiddenSize, actionSpace)

  /** Gets the current policy */
  private def getPolicy: State => Action = {
    agentMode match {
      case AgentMode.Training => learner.behavioural
      case AgentMode.Testing => testPolicy
    }
  }

}