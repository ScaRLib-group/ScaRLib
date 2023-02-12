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

  private var dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private var epsilon: Decay[Double] = ExponentialDecay(0.9, 0.1) // TODO - aggiungi metodo bounded a decay
  private var learner: DeepQLearner = DeepQLearner(dataset, actionSpace, epsilon, 0.9, 0.0005, inputSize = 3)(Random(42)) //TODO migliora inputsize

  def loop: Unit =
      while checkReward do
        println(s"Agent: ${agentId} --- before step")
        val state = environment.observe(agentId)
        println(s"Agent: ${agentId} --- after observe")
        val policy = learner.behavioural
        val action = policy(state)
        println(s"Agent: ${agentId} --- after policy")
        val result: (Double, State) = environment.step(action, agentId)
        println(s"Agent: ${agentId} --- reward: ${result._1}")
        dataset.insert(state, action, result._1, result._2)
        learner.improve()
        epsilon.update

  private def checkReward: Boolean = !(dataset.size > previousExperience && meanReward > targetRewardLB && meanReward < targetRewardUB)
  private def meanReward: Double = average(dataset.getAll.take(previousExperience).map(_.reward))
  private def average(s: Seq[Double]): Double = s.foldLeft((0.0, 1)) ((acc, i) => ((acc._1 + (i - acc._1) / acc._2), acc._2 + 1))._1