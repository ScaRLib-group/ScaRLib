package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.reflect.io.File
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class DecentralizedAgent(
                          agentId: Int,
                          environment: Environment,
                          actionSpace: Seq[Action],
                          datasetSize: Int,
                          agentMode: AgentMode = AgentMode.Training,
                          learningConfiguration: LearningConfiguration
) {

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val epsilon: Decay[Double] = learningConfiguration.epsilon
  private val learner: DeepQLearner = new DeepQLearner(dataset, actionSpace, learningConfiguration)(new Random(42))
  private val posLogs: StringBuilder = new StringBuilder()
  private var testPolicy: State => Action = _

  def step(): Future[Unit] = {
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
    testPolicy = DeepQLearner.policyFromNetworkSnapshot(p.path, p.inputSize, p.hiddenSize, actionSpace)

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