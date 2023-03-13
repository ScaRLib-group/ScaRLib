package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.annotation.tailrec

import scala.concurrent.ExecutionContext
import scala.concurrent.{Await, Future}
import scala.util.Random

class CTDESystem(
                  agents: Seq[IndependentAgent],
                  environment: Environment,
                  dataset: ReplayBuffer[State, Action],
                  actionSpace: Seq[Action],
                  learningConfiguration: LearningConfiguration
)(implicit context: ExecutionContext) {

  private val epsilon: Decay[Double] = learningConfiguration.epsilon
  private val learner: DeepQLearner =
    new DeepQLearner(dataset, actionSpace, learningConfiguration)(new Random(42))

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
