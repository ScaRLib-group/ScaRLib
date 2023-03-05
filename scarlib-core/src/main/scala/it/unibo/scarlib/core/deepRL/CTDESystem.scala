package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model.*

import scala.annotation.tailrec
import scala.util.Random

class CTDESystem(
    agents: Seq[IndipendentAgent],
    dataset: ReplayBuffer[State, Action],
    actionSpace: Seq[Action],
    environment: GeneralEnvironment
):
  var inputSize = (agents.length - 1) * 2 + 5 * 2
  println("inputSize: " + inputSize)
  private val epsilon: Decay[Double] = ExponentialDecay(0.9, 0.01, 0.01)
  private val learner: DeepQLearner =
    DeepQLearner(dataset, actionSpace, epsilon, 0.99, 0.0005, inputSize = inputSize)(Random(42))
  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit =
    @tailrec
    def singleEpisode(time: Int): Unit =
      if time > 0 then
        agents.foreach(_.notifyNewPolicy(learner.behavioural))
        agents.foreach(_.step)
        environment.log
        learner.improve()
        singleEpisode(time - 1)

    if episodes > 0 then
      singleEpisode(episodeLength)
      epsilon.update
      environment.reset
      learner.snapshot(episodes, 0)
      learn(episodes - 1, episodeLength)
    else
      agents.foreach(_.logOnFile)
      environment.logOnFile
