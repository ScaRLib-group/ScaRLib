package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.{SimpleSequentialDQN, TorchSupport}
import it.unibo.scarlib.core.util.TorchLiveLogger
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.{PyQuote, SeqConverters}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Random

class DeepQLearner(memory: ReplayBuffer[State, Action],
                   actionSpace: Seq[Action],
                   learningConfiguration: LearningConfiguration)(implicit random: Random) {

  private val learningRate = learningConfiguration.learningRate
  private val epsilon = learningConfiguration.epsilon
  private val batchSize = learningConfiguration.batchSize
  private val gamma = learningConfiguration.gamma
  private val updateEach = learningConfiguration.updateEach
  private var updates = 0
  private val device = AutodiffDevice()
  private val targetNetwork = learningConfiguration.dqnFactory.createNN().asInstanceOf[py.Dynamic]
  private val policyNetwork = learningConfiguration.dqnFactory.createNN().asInstanceOf[py.Dynamic]
  private val targetPolicy = DeepQLearner.policyFromNetwork(policyNetwork, actionSpace)
  private val behaviouralPolicy = DeepQLearner.policyFromNetwork(policyNetwork, actionSpace)
  private val optimizer = TorchSupport.optimizerModule().RMSprop(policyNetwork.parameters(), learningRate)
  
  val optimal: State => Action = targetPolicy

  val behavioural: State => Action =
    state =>
      if (random.nextDouble() < epsilon.value()) {
        random.shuffle(actionSpace).head
      } else {
        behaviouralPolicy(state)
      }
  
  def record(state: State, action: Action, reward: Double, nextState: State): Unit =
    memory.insert(state, action, reward, nextState)

  def improve(): Unit = { //if (this.mode == AgentMode.Training) {
    val memorySample = memory.subsample(batchSize)
    if (memorySample.size == batchSize) {
      val states = memorySample.map(_.actualState).map(state => state.toSeq().toPythonCopy).toPythonCopy
      val action = memorySample.map(_.action).map(action => actionSpace.indexOf(action)).toPythonCopy
      val rewards = TorchSupport.deepLearningLib().tensor(memorySample.map(_.reward).toPythonCopy).to(device)
      val nextState = memorySample.map(_.nextState).map(state => state.toSeq().toPythonCopy).toPythonCopy
      val stateActionValue = policyNetwork(TorchSupport.deepLearningLib().tensor(states).to(device))
        .gather(1, TorchSupport.deepLearningLib().tensor(action).to(device).view(batchSize, 1))
      val nextStateValues =
        targetNetwork(TorchSupport.deepLearningLib().tensor(nextState).to(device)).max(1).bracketAccess(0).detach()
      val expectedValue = (nextStateValues * gamma) + rewards
      val criterion = TorchSupport.neuralNetworkModule().SmoothL1Loss()
      val loss = criterion(stateActionValue, expectedValue.unsqueeze(1))
      TorchLiveLogger.logScalar("Loss", loss.item().as[Double], updates)
      optimizer.zero_grad()
      loss.backward()
      it.unibo.scarlib.core.neuralnetwork.TorchSupport
        .neuralNetworkModule()
        .utils
        .clip_grad_value_(policyNetwork.parameters(), 1.0)
      optimizer.step()
      updates += 1
      if (updates % updateEach == 0) {
        targetNetwork.load_state_dict(policyNetwork.state_dict())
      }
    }
  }

  def snapshot(episode: Int, agentId: Int): Unit = {
    val timeMark = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date)
    TorchSupport
      .deepLearningLib()
      .save(
        targetNetwork.state_dict(),
        s"../data/network-$episode-$timeMark-agent-$agentId"
      )
  }
}

object DeepQLearner {
  def policyFromNetworkSnapshot[S <: State, A](
      path: String,
      inputSize: Int,
      hiddenSize: Int,
      actionSpace: Seq[A]
  ): S => A = {
    val model = SimpleSequentialDQN(inputSize, hiddenSize, actionSpace.size)
    model.load_state_dict(TorchSupport.deepLearningLib().load(path))
    policyFromNetwork(model, actionSpace)
  }

  def policyFromNetwork[S <: State, A](network: py.Dynamic, actionSpace: Seq[A]): S => A = { state =>
    val netInput = state.toSeq()
    py.`with`(TorchSupport.deepLearningLib().no_grad()) { _ =>
      val tensor =
        TorchSupport.deepLearningLib().tensor(netInput.toPythonCopy).to(AutodiffDevice()).view(1, state.elements())
      val actionIndex = network(tensor).max(1).bracketAccess(1).item().as[Int]
      actionSpace(actionIndex)
    }
  }
}
