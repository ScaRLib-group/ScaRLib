package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.annotation.tailrec

import scala.concurrent.ExecutionContext
import scala.concurrent.{Await, Future}
import scala.util.Random

class CTDESystem(
                  agents: Seq[IndipendentAgent],
                  dataset: ReplayBuffer[State, Action],
                  actionSpace: Seq[Action],
                  environment: Environment,
                  inputSize: Int = 10
)(implicit context: ExecutionContext) {
  private val epsilon: Decay[Double] = new ExponentialDecay(0.9, 0.1, 0.01)
  private val learner: DeepQLearner =
    new DeepQLearner(dataset, actionSpace, epsilon, 0.90, 0.0005, hiddenSize = 64, inputSize = inputSize)(
      new Random(42)
    ) //TODO migliora inputsize


  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit =
      if (time > 0) {
        agents.foreach(_.notifyNewPolicy(learner.behavioural))
        Await.ready(Future.sequence(agents.map(_.step())), scala.concurrent.duration.Duration.Inf)
        environment.log()
        learner.improve()
        singleEpisode(time - 1)
      }

    if (episodes > 0) {
      println("Episode: " + episodes)
      singleEpisode(episodeLength)
      epsilon.update()
      environment.reset()
      learner.snapshot(episodes, 0)
      learn(episodes - 1, episodeLength)
    } else {
      agents.foreach(_.logOnFile())
      environment.logOnFile()
    }

  }

  final def runTest(episodeLength: Int, policy: PolicyNN): Unit = {
    val p: State => Action =
      DeepQLearner.policyFromNetworkSnapshot(policy.path, policy.inputSize, policy.hiddenSize, actionSpace)
    agents.foreach(_.notifyNewPolicy(p))
    episode(episodeLength)

    @tailrec
    def episode(time: Int): Unit = {
      if (time > 0) {
        Await.ready(Future.sequence(agents.map(_.step())), scala.concurrent.duration.Duration.Inf)
        episode(time - 1)
      }
    }
  }

}
