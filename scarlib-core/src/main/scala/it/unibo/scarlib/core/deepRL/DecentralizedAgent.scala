package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model.{Action, Decay, DeepQLearner, ExponentialDecay, ReplayBuffer, State, GeneralEnvironment}
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding

import scala.collection.immutable.Seq
import scala.util.Random

class DecentralizedAgent(
            agentId: Int,
            environment: GeneralEnvironment,
            datasetSize: Int,
            actionSpace: Seq[Action],
            targetRewardLB: Double,
            targetRewardUB: Double,
            previousExperience: Int
            ):

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val epsilon: Decay[Double] = ExponentialDecay(0.9, 0.1, 0.01)
  private val learner: DeepQLearner = DeepQLearner(dataset, actionSpace, epsilon, 0.9, 0.0005, inputSize = 4)(Random(42)) //TODO migliora inputsize

  def step: Unit =
    println(s"Agent: ${agentId} --- init")
    val state = environment.observe(agentId)
    val policy = learner.behavioural
    val action = policy(state)
    println(s"Agent: ${agentId} --- choosen action: $action")
    val result: (Double, State) = environment.step(action, agentId)
    println(s"Agent: ${agentId} --- reward: ${result._1}")
    dataset.insert(state, action, result._1, result._2)
    println(s"Agent: ${agentId} --- calling improve")
    learner.improve()
    println(s"Agent: ${agentId} --- called improve")
    epsilon.update
    println(s"Agent: ${agentId} --- updated epsilon")

  private def checkReward: Boolean = !(dataset.size > previousExperience && meanReward > targetRewardLB && meanReward < targetRewardUB)
  private def meanReward: Double = average(dataset.getAll.take(previousExperience).map(_.reward))
  private def average(s: Seq[Double]): Double = s.foldLeft((0.0, 1)) ((acc, i) => ((acc._1 + (i - acc._1) / acc._2), acc._2 + 1))._1