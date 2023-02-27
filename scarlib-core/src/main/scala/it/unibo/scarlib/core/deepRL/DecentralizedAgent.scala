package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.reflect.io.File
import scala.util.Random

class DecentralizedAgent(
                          agentId: Int,
                          environment: GeneralEnvironment,
                          datasetSize: Int,
                          actionSpace: Seq[Action],
                          agentMode: AgentMode = AgentMode.Training) {

    private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
    private val epsilon: Decay[Double] = new ExponentialDecay(0.9, 0.1, 0.01)
    private val learner: DeepQLearner = new DeepQLearner(dataset, actionSpace, epsilon, 0.9, 0.0005, inputSize = 6)(new Random(42)) // TODO migliora inputsize
    private val posLogs: StringBuilder = new StringBuilder()

    def step(): Unit = {
        val state = environment.observe(agentId)
        val policy = getPolicy()
        val action = policy(state)
        val result: (Double, State) = environment.step(action, agentId)
        //logPos(result._2.asInstanceOf[MyState].agentPosition)
        dataset.insert(state, action, result._1, result._2)
        learner.improve()
        epsilon.update()
    }

    private def getPolicy(): State => Action = {
      agentMode match {
        case AgentMode.Training => learner.behavioural
        case AgentMode.Testing => ??? // TODO -load policy from snapshot
      }
    }

    private def logPos(pos: (Double, Double)): Unit = {
        posLogs.append(pos.toString + "\n")
    }

    def logOnFile(): Unit = {
        val file = File(s"agent-${agentId}.txt")
        val bw = file.bufferedWriter(append = true)
        bw.write(posLogs.toString())
        bw.close()
    }

    def snapshot(episode: Int): Unit = { learner.snapshot(episode, agentId) }

}

