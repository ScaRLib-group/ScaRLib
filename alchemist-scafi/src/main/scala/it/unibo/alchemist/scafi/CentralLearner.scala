//package it.unibo.alchemist.scafi
//
//import it.unibo.alchemist.model.interfaces.{Environment, Position, Time, TimeDistribution}
//import it.unibo.scarlib.core.model.{Action, DeepQLearner, ExponentialDecay, ReplayBuffer, Result, RewardFunction, State}
//import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
//import it.unibo.scarlib.core.util.TorchLiveLogger
//
//import scala.util.Random
//
//class CentralLearner[T, P <: Position[P]](
//                                           environment: Environment[T, P],
//                                           distribution: TimeDistribution[T],
//                                           deltaMovement: Double,
//                                           targetDistance: Double,
//                                           val rewardFunction: RewardFunction,
//                                           val actionSpace: Seq[Action],
//                                           inputSize: Int
//                                         ) extends AbstractReaction[T, P](environment, distribution) {
//    private var memory: Seq[Result] = List.empty // used to store the last collective experience
//    private var initialPosition: List[P] = List.empty[P] // used to restart the simulation with the same configuration
//    private var updates = 0
//    private val epsilon = ExponentialDecay(0.9, 0.10)//.bounded(0.01)
//
//    val learner = new DeepQLearner(
//        ReplayBuffer[State,Action](100000),
//        actionSpace,
//        epsilon,
//        0.90,
//        0.0005,
//        batchSize = 64,
//        inputSize = inputSize
//    )(new Random())
//
//    //learner.trainingMode()
//
//    override def executeBeforeUpdateDistribution(): Unit = {
//        if (environment.getSimulation.getTime.toDouble > 1) { // skip the first tick
//            val stateAndActions = stateAndAction
//            val actions = stateAndActions.map(_.action)
//            val states = stateAndActions.map(_.state)
//            improvePolicy(states)
//            memory = stateAndActions
//            CollectiveAction.moveAll(this, deltaMovement)
//        }
//    }
//
//     override def initializationComplete(time: Time, environment: Environment[T, _]): Unit = ???
//        //initialPosition = agents.map(this.environment.getPosition)
//
////    def improvePolicy(states: Seq[State]): Unit = {
////        val evalReward = rewardFunction.compute(states, updates)
////        val totalReward = evalReward.sum
////        TorchLiveLogger.logScalar("Reward", totalReward, updates)
////        if (memory.nonEmpty) {
////            updates += 1
////            memory.zip(evalReward).zip(states).foreach { case ((Result(state, action), reward), nextState) =>
////                learner.record(state, action, reward, nextState)
////            }
////            learner.improve()
////            if (updates % learner.updateEach == 0) {
////                memory = List.empty
////                agents.zip(initialPosition).foreach { case (agent, position) =>
////                    environment.moveNodeToPosition(agent, position)
////                }
////                // decay here
////                epsilon.update
////                TorchLiveLogger.logScalar("Epsilon", epsilon.value, (updates / learner.updateEach).toInt)
////                learner.snapshot(updates / learner.updateEach)
////            }
////        }
////    }
//}