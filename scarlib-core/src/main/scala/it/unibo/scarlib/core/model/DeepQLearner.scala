package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.{DeepLearningSupport, NeuralNetworkEncoding, SimpleSequentialDQN, TorchSupport}
import it.unibo.scarlib.core.util.TorchLiveLogger
import me.shadaj.scalapy.py


import scala.util.Random
import java.text.SimpleDateFormat
import java.util.Date
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}

class DeepQLearner[State, Action](
                                   memory: ReplayBuffer[State, Action],
                                   actionSpace: Seq[Action],
                                   var epsilon: Decay[Double],
                                   gamma: Double,
                                   learningRate: Double,
                                   batchSize: Int = 32,
                                   val updateEach: Int = 100,
                                   val hiddenSize: Int = 32,
                                   val _agentMode: AgentMode = AgentMode.Training
                                 )(implicit encoding: NeuralNetworkEncoding[State], random: Random)
  extends Agent {
    private var updates = 0
    private val targetNetwork = SimpleSequentialDQN(encoding.elements, hiddenSize, actionSpace.size)
    private val policyNetwork = SimpleSequentialDQN(encoding.elements, hiddenSize, actionSpace.size)
    private val targetPolicy = DeepQLearner.policyFromNetwork(policyNetwork, encoding, actionSpace)
    private val behaviouralPolicy = DeepQLearner.policyFromNetwork(policyNetwork, encoding, actionSpace)
    private val optimizer = TorchSupport.optimizerModule.RMSprop(policyNetwork.parameters(), learningRate)

    val optimal: State => Action = targetPolicy

    val behavioural: State => Action = state =>
        if (random.nextDouble() < epsilon.value) {
            random.shuffle(actionSpace).head
        } else behaviouralPolicy(state)

    override def mode: AgentMode = _agentMode

    def record(state: State, action: Action, reward: Double, nextState: State): Unit =
        memory.insert(state, action, reward, nextState)

    def improve(): Unit = if (this.mode == AgentMode.Training) {
        val memorySample = memory.subsample(batchSize)
        if (memory.subsample(batchSize).size == batchSize) {
            val states = memorySample.map(_.actualState).toSeq.map(state => encoding.toSeq(state).toPythonCopy).toPythonCopy //TODO ActualState o NextState?
            val action = memorySample.map(_.action).toSeq.map(action => actionSpace.indexOf(action)).toPythonCopy
            val rewards = TorchSupport.deepLearningLib.tensor(memorySample.map(_.reward).toSeq.toPythonCopy)
            val nextState = memorySample.map(_.nextState).toSeq.map(state => encoding.toSeq(state).toPythonCopy).toPythonCopy
            val stateActionValue = policyNetwork(TorchSupport.deepLearningLib.tensor(states)).gather(1, TorchSupport.deepLearningLib.tensor(action).view(batchSize, 1))
            val nextStateValues = targetNetwork(TorchSupport.deepLearningLib.tensor(nextState)).max(1).bracketAccess(0).detach()
            val expectedValue = (nextStateValues * gamma) + rewards
            val criterion = TorchSupport.neuralNetworkModule.SmoothL1Loss()
            val loss = criterion(stateActionValue, expectedValue.unsqueeze(1))
            TorchLiveLogger.logScalar("Loss", loss.item().as[Double], updates)
            optimizer.zero_grad()
            loss.backward()
            py"[param.grad.data.clamp_(-1, 1) for param in ${policyNetwork.parameters()}]"
            optimizer.step()
            updates += 1
            if (updates % updateEach == 0) {
                targetNetwork.load_state_dict(policyNetwork.state_dict())
            }
        }
    }

    def snapshot(episode: Int): Unit = {
        val timeMark = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date)
        TorchSupport.deepLearningLib.save(targetNetwork.state_dict(), s"data/network-$episode-$timeMark")
    }
}

object DeepQLearner {
    def policyFromNetworkSnapshot[S, A](
                                         path: String,
                                         hiddenSize: Int,
                                         encoding: NeuralNetworkEncoding[S],
                                         actionSpace: Seq[A]
                                       ): S => A = {
        val model = SimpleSequentialDQN(encoding.elements, hiddenSize, actionSpace.size)
        model.load_state_dict(TorchSupport.deepLearningLib.load(path))
        policyFromNetwork(model, encoding, actionSpace)
    }

    def policyFromNetwork[S, A](network: py.Dynamic, encoding: NeuralNetworkEncoding[S], actionSpace: Seq[A]): S => A = {
        state =>
            val netInput = encoding.toSeq(state)
            py.`with`(TorchSupport.deepLearningLib.no_grad()) { _ =>
                val tensor = TorchSupport.deepLearningLib.tensor(netInput.toPythonCopy).view(1, encoding.elements)
                val actionIndex = network(tensor).max(1).bracketAccess(1).item().as[Int]
                actionSpace(actionIndex)
            }
    }
}