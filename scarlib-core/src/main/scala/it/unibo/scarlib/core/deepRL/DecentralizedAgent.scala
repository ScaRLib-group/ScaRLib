/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._
import scala.reflect.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class DecentralizedAgent(
                          agentId: Int,
                          environment: Environment,
                          actionSpace: Seq[Action],
                          datasetSize: Int,
                          agentMode: AgentMode = AgentMode.Training,
                          learningConfiguration: LearningConfiguration
) extends Agent {

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val epsilon: Decay[Double] = learningConfiguration.epsilon
  private val learner: DeepQLearner = new DeepQLearner(dataset, actionSpace, learningConfiguration)
  private val posLogs: StringBuilder = new StringBuilder()
  private var testPolicy: State => Action = _

  override def step(): Future[Unit] = {
    val state = environment.observe(agentId)
    val policy = getPolicy
    val action = policy(state)
    environment
      .step(action, agentId)
      .map { result =>
        agentMode match {
          case AgentMode.Training =>
            dataset.insert(state, action, result._1, result._2)
            learner.improve()
            epsilon.update()
          case AgentMode.Testing => //do nothing
        }
      }
  }

  def snapshot(episode: Int): Unit = learner.snapshot(episode, agentId)

  def setTestPolicy(p: PolicyNN): Unit =
    testPolicy = DeepQLearner.policyFromNetworkSnapshot(p.path + s"-$agentId", p.inputSize, p.hiddenSize, actionSpace)

  private def getPolicy: State => Action = {
    agentMode match {
      case AgentMode.Training => learner.behavioural
      case AgentMode.Testing => testPolicy
    }
  }

  private def logPos(pos: (Double, Double)): Unit =
    posLogs.append(pos.toString + "\n")

  def logOnFile(): Unit = {
    val file = File(s"agent-$agentId.txt")
    val bw = file.bufferedWriter(append = true)
    bw.write(posLogs.toString())
    bw.close()
  }

}