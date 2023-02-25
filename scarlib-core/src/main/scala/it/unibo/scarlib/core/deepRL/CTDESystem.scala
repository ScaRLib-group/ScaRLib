package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.annotation.tailrec
import scala.util.Random

class CTDESystem(agents: Seq[IndipendentAgent], dataset: ReplayBuffer[State, Action], actionSpace: Seq[Action], environment: GeneralEnvironment){
  private val epsilon: Decay[Double] = new ExponentialDecay(0.9, 0.1, 0.01)
  private val learner: DeepQLearner = new DeepQLearner(dataset, actionSpace, epsilon, 0.9, 0.0005, inputSize = 4)(new Random(42)) //TODO migliora inputsize

  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit =
      if (time > 0) {
        agents.foreach(_.notifyNewPolicy(learner.behavioural))
        agents.foreach(_.step)
        environment.log
        learner.improve()
        singleEpisode(time - 1)
      }

    if (episodes > 0) {
      singleEpisode(episodeLength)
      epsilon.update
      environment.reset
      learner.snapshot(episodes, 0)
      learn(episodes - 1, episodeLength)
    } else {
      agents.foreach(_.logOnFile)
      environment.logOnFile
    }

  }

}

