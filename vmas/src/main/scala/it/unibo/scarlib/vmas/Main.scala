package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.deepRL.CTDESystem
import it.unibo.scarlib.core.model.{Action, DeepQLearner, LearningConfiguration, ReplayBuffer, State}
import it.unibo.scarlib.core.util.Logger

import scala.concurrent.ExecutionContext

class Main extends App {

    val stateDescriptor = VMASStateDescriptor(lidars = Seq[Int](15,15))
    private val memory = ReplayBuffer[VMASState, VMASAction](10000)
    private val actions = VMASAction.toSeq
    private val learningConfiguration = new LearningConfiguration(
        snapshotPath = "snapshot/",
        dqnFactory = new NNFactory(stateDescriptor, actions)
    )
    val dql = new DeepQLearner(memory.asInstanceOf[ReplayBuffer[State, Action]], actions, learningConfiguration)
    val rewardFunction = new CohisionAndCollisionRewardFunction()
    private val envSettings = new VmasSettings(scenario = "CohisionAndCollision", nEnv = 2, nAgents = 4, nTargets = 0, nSteps = 100)
    val environment = new VmasEnvironment(rewardFunction, actions, envSettings, WANDBLogger.getClass.asInstanceOf[Class[_ <: Logger]])
    val nAgents = 4
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
        learningConfiguration = learningConfiguration
    )(ExecutionContext.global)
    ctde.learn(envSettings.nSteps, envSettings.nEpochs)

}
