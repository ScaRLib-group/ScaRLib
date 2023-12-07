package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.EmptyState.encoding
import it.unibo.scarlib.core.system.CTDESystem
import it.unibo.scarlib.core.model.{Action, DeepQLearner, LearningConfiguration, ReplayBuffer, State}
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
import it.unibo.scarlib.core.util.Logger
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.concurrent.ExecutionContext

object Main extends App {
//    py.module("sys").path.append("src/main/resources/Cleaning.py")
//    py"import sys".call()
//    py"sys.path.append('src/main/resources')".call()
//    py"print('ciao')".call()
//    py"import Cleaning".call()
//    py"from Cleaning import CleaningScenario".call()
//    val scenario = py"CleaningScenario()".call()
    val scenario = py.module("Cleaning").CleaningScenario()
    val stateDescriptor = VMASStateDescriptor(hasPosition=false, hasVelocity=false, lidars = Seq[Int](50))
    VMASState.setDescriptor(stateDescriptor)
    private val memory = ReplayBuffer[VMASState, VMASAction](10000)
    private val actions = VMASAction.toSeq
    private val learningConfiguration = new LearningConfiguration(
        snapshotPath = "snapshot/",
        dqnFactory = new NNFactory(stateDescriptor, actions)
    )
    val rewardFunction = new CohisionAndCollisionRewardFunction()
    val nAgents = 1
    private val envSettings = VmasSettings(scenario = scenario, nEnv = 1, nAgents = nAgents, nTargets = 8,
        nSteps = 1000, nEpochs = 150, device = "cpu")
    //WANDBLogger.login()
    WANDBLogger.init()
    val environment = new VmasEnvironment(rewardFunction, actions, envSettings, WANDBLogger, render = true)
    var agents = Seq[VMASAgent]()
    for (i <- 0 until nAgents) {
        val agent = new VMASAgent(environment, actions, memory.asInstanceOf[ReplayBuffer[State, Action]])
        agents = agents :+ agent
    }
    val ctde = new CTDESystem(
        agents = agents,
        environment = environment,
        dataset = memory.asInstanceOf[ReplayBuffer[State, Action]],
        actionSpace = actions,
        learningConfiguration = learningConfiguration,
        logger = WANDBLogger
    )(ExecutionContext.global, VMASState.encoding)
    ctde.learn(envSettings.nEpochs, envSettings.nSteps)
}
