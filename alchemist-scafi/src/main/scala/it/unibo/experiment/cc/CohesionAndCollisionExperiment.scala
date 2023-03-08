package it.unibo.experiment.cc

import ch.qos.logback.classic.Level
import it.unibo.alchemist.loader.m2m.{JVMConstructor, SimulationModel}
import it.unibo.alchemist.{AlchemistEnvironment, ShowEach}
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.{Action, ReplayBuffer, RewardFunction, State}
import it.unibo.scarlib.core.util.AgentGlobalStore
import org.slf4j.LoggerFactory

object CCActions {
  final case object North extends Action
  final case object South extends Action
  final case object East extends Action
  final case object West extends Action
  final case object NorthEast extends Action
  final case object NorthWest extends Action
  final case object SouthWest extends Action
  final case object SouthEast extends Action

  def toSeq(): Seq[Action] = Seq(North, South, East, West, NorthEast, NorthWest, SouthEast, SouthWest)
}

case class CCState(positions: List[(Double, Double)], agentId: Int) extends State {
  override def elements(): Int = 5 * 2

  override def toSeq(): Seq[Double] = {
    val fill = List.fill(elements())(0.0)
    (positions.flatMap { case (l, r) => List(l, r) } ++ fill).take(elements())
  }

  override def isEmpty(): Boolean = false
}

class CCRewardFunction() extends RewardFunction {

  private val targetDistance = 0.2
  private var ticks: Int = 0

  override def compute(currentState: State, action: Action, newState: State): Double = {
    ticks += 1
    if (currentState.isEmpty()) {
      0.0
    } else {
      val s = newState.asInstanceOf[CCState]
      val distances = computeDistancesFromNeighborhood(s)
      if (distances.isEmpty) {
        0.0
      } else {
        val cohesion = cohesionFactor(distances)
        val collision = collisionFactor(distances)
        AgentGlobalStore().put(s.agentId, "cohesion", cohesion)
        AgentGlobalStore().put(s.agentId, "collision", collision)
        AgentGlobalStore().put(s.agentId, "reward", collision + cohesion)
        cohesion + collision
      }
    }
  }

  private def cohesionFactor(distances: Seq[Double]): Double = {
    val max: Double = distances.max
    if (max > targetDistance) { -(max - targetDistance) }
    else { 0.0 }
  }

  private def collisionFactor(distances: Seq[Double]): Double = {
    val min: Double = distances.min
    if (min < targetDistance) { 2 * math.log(min / targetDistance) }
    else { 0.0 }
  }

  private def computeDistancesFromNeighborhood(state: CCState): Seq[Double] =
    state.positions.map(p => Point3D.Zero.distance(Point3D(p._1, p._2, 0)))
}

object CohesionAndCollisionExperiment extends App {

  private val rewardFunction = new CCRewardFunction()
  LoggerFactory.getLogger(classOf[SimulationModel]).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(Level.OFF)
  LoggerFactory.getLogger(classOf[JVMConstructor]).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(Level.OFF)

  val env = new AlchemistEnvironment(
    "./alchemist-scafi/src/main/scala/it/unibo/experiment/cc/CohesionAndCollisionSim.yaml",
    rewardFunction,
    CCActions.toSeq(),
    new ShowEach(100)
  )
  val datasetSize = 10000

  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)

  private var agents: Seq[IndipendentAgent] = Seq.empty
  for (n <- 0 to 49)
    agents = agents :+ new IndipendentAgent(env, n, dataset, CCActions.toSeq())
  new CTDESystem(agents, dataset, CCActions.toSeq(), env).learn(1000, 100)

  /*private var agents: Seq[DecentralizedAgent] = Seq.empty
  for (n <- 0 to 49)
    agents = agents :+ new DecentralizedAgent(n, env, 10000, CCActions.toSeq())

  new DTDESystem(agents, env).learn(1000, 100)*/
}
