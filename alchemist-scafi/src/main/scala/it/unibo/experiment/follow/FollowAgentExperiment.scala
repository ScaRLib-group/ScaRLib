package it.unibo.experiment.follow

import ch.qos.logback.classic.Level
import it.unibo.alchemist.loader.m2m.{JVMConstructor, SimulationModel}
import it.unibo.alchemist.{AlchemistEnvironment, ShowEach}
import it.unibo.experiment.cc.CCActions
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.{Action, ReplayBuffer, RewardFunction, State}
import it.unibo.scarlib.core.util.AgentGlobalStore
import org.slf4j.LoggerFactory

class FollowRewardFunction() extends RewardFunction {

  private val targetDistance = 0.2
  private var ticks: Int = 0

  override def compute(currentState: State, action: Action, newState: State): Double = {
    ticks += 1
    if (currentState.isEmpty()) {
      0.0
    } else {
      val s = newState.asInstanceOf[FollowAgentState]
      val distances = computeDistancesFromNeighborhood(s)
      if (distances.isEmpty) {
        0.0
      } else {
        val collision = collisionFactor(distances)
        AgentGlobalStore().put(s.id, "cohesion", -s.distanceToLeader)
        AgentGlobalStore().put(s.id, "collision", collision)
        AgentGlobalStore().put(s.id, "reward", collision - s.distanceToLeader)
        -s.distanceToLeader + collision
      }
    }
  }

  private def collisionFactor(distances: Seq[Double]): Double = {
    val min: Double = distances.min
    if (min < targetDistance) { 2 * math.log(min / targetDistance) }
    else { 0.0 }
  }

  private def computeDistancesFromNeighborhood(state: FollowAgentState): Seq[Double] =
    state.distances.map(p => Point3D.Zero.distance(Point3D(p._1, p._2, 0)))
}

object FollowLeaderExperiment extends App {

  private val rewardFunction = new FollowRewardFunction()
  LoggerFactory.getLogger(classOf[SimulationModel]).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(Level.OFF)
  LoggerFactory.getLogger(classOf[JVMConstructor]).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(Level.OFF)

  val env = new AlchemistEnvironment(
    "./src/main/scala/it/unibo/experiment/follow/FollowSim.yaml",
    rewardFunction,
    CCActions.toSeq(),
    new ShowEach(100)
  )
  val datasetSize = 10000

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)

  private var agents: Seq[IndipendentAgent] = Seq.empty
  for (n <- 0 until env.currentNodeCount)
    agents = agents :+ new IndipendentAgent(env, n, dataset, CCActions.toSeq())
  new CTDESystem(agents, dataset, CCActions.toSeq(), env, inputSize = 12).learn(1000, 100)

  /*private var agents: Seq[DecentralizedAgent] = Seq.empty
  for (n <- 0 to 49)
    agents = agents :+ new DecentralizedAgent(n, env, 10000, CCActions.toSeq())

  new DTDESystem(agents, env).learn(1000, 100)*/
}
