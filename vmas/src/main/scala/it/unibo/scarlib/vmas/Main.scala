package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.EmptyState.encoding
import it.unibo.scarlib.core.system.{CTDEAgent, CTDESystem}
import it.unibo.scarlib.core.model.{Action, DeepQLearner, Environment, LearningConfiguration, ReplayBuffer, State}
import it.unibo.scarlib.core.neuralnetwork.{NeuralNetworkEncoding, NeuralNetworkSnapshot}
import it.unibo.scarlib.core.util.Logger
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.concurrent.ExecutionContext

object Main extends App {
    val scenario = py.module("CohesionAndCollisionNoLidar").Scenario()
    val nNeighbour = 5
    val stateDescriptor = VmasStateDescriptor(hasPosition=true, hasVelocity=false, extraDimension = nNeighbour * 2)
    VMASState.setDescriptor(stateDescriptor)
    private val memory = ReplayBuffer[VMASState, VMASAction](10000)
    private val actions = VMASAction.toSeq
    private val learningConfiguration = new LearningConfiguration(
        snapshotPath = "snapshot",
        dqnFactory = new NNFactory(stateDescriptor, actions)
    )
    val nAgents = 100
    private val envSettings = VmasSettings(scenario = scenario, nEnv = 1, nAgents = nAgents, nTargets = 0,
        nSteps = 1000, nEpochs = 150, device = "cuda", neighbours = 5)
    WANDBLogger.init()
    val environment = new VmasEnvironment(null, actions, envSettings, WANDBLogger, render = true)
    var agents = Seq[CTDEAgent]()
    for (i <- 0 until nAgents) {
        val agent = new CTDEAgent(agentId = i, environment, actions, memory.asInstanceOf[ReplayBuffer[State, Action]])
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
    //ctde.learn(envSettings.nEpochs, envSettings.nSteps)
    ctde.runTest(100, NeuralNetworkSnapshot("cohesion-collision-snapshot", stateDescriptor.getSize, 64))



//    implicit val configuration: Environment => Unit = (e: Environment) => {
//        val env = e.asInstanceOf[VmasEnvironment]
//
//    }
}
