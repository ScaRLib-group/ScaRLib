package it.unibo.experiment.cc

import it.unibo.alchemist.AlchemistEnvironment
import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.{Action, ReplayBuffer, RewardFunction, State}
import it.unibo.scarlib.core.util.TorchLiveLogger

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

case class CCState(positions: List[(Double, Double)], agentPosition: (Double, Double), agentId: Int) extends State {
  override def elements(): Int = 3 * 2

  override def toSeq(): Seq[Double] = {
    val fill = List.fill(elements())(0.0)
    (positions.flatMap { case (l, r) => List(l, r) } ++ fill).take(elements())
  }
}

class CCRewardFunction() extends RewardFunction {

  private val targetDistance = 0.2
  private var ticks: Int = 0

  override def compute(currentState: State, action: Action, newState: State): Double = {
    ticks += 1
    val s = newState.asInstanceOf[CCState]
    val distances = computeDistancesFromNeighborhood(s)
    val cohesion = cohesionFactor(distances)
    val collision = collisionFactor(distances)
    val t = (ticks / 25.0).floor.toInt
    TorchLiveLogger.logScalar(s"Cohesion reward - agent${s.agentId}", cohesion, t)
    TorchLiveLogger.logScalar(s"Collision reward - agent${s.agentId}", collision, t)
    cohesion + collision
  }

  private def cohesionFactor(distances: Seq[Double]): Double = {
    val max: Double = distances.max
    if (max > targetDistance) {-(max - targetDistance)} else { 0.0 }
  }

  private def collisionFactor(distances: Seq[Double]): Double = {
    val min: Double = distances.min
    if (min < targetDistance) {2 * math.log(min / targetDistance)} else {0.0}
  }

  private def euclideanDistance(x: (Double, Double), y: (Double, Double)): Double = {
    Math.sqrt(Math.pow((x._1 - y._1), 2) + Math.pow((x._2 - y._2), 2))
  }

  private def computeDistancesFromNeighborhood(state: CCState): Seq[Double] = {
    state.positions.map(p => euclideanDistance(p, state.agentPosition))
  }
}

object CohesionAndCollisionExperiment extends App {

  private val rewardFunction = new CCRewardFunction()

  val env = new AlchemistEnvironment(
    "???.yaml",
    rewardFunction,
    CCActions.toSeq())

  val datasetSize = 10000
  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val agents: Seq[IndipendentAgent] = Seq(
    new IndipendentAgent(env, 0, dataset),
    new IndipendentAgent(env, 1, dataset),
    new IndipendentAgent(env, 2, dataset),
    new IndipendentAgent(env, 3, dataset),
    new IndipendentAgent(env, 4, dataset),
    new IndipendentAgent(env, 5, dataset),
    new IndipendentAgent(env, 6, dataset),
    new IndipendentAgent(env, 7, dataset),
    new IndipendentAgent(env, 8, dataset),
    new IndipendentAgent(env, 9, dataset),
    new IndipendentAgent(env, 10, dataset),
    new IndipendentAgent(env, 11, dataset),
    new IndipendentAgent(env, 12, dataset),
    new IndipendentAgent(env, 13, dataset),
    new IndipendentAgent(env, 14, dataset),
    new IndipendentAgent(env, 15, dataset),
    new IndipendentAgent(env, 16, dataset),
    new IndipendentAgent(env, 17, dataset),
    new IndipendentAgent(env, 18, dataset),
    new IndipendentAgent(env, 19, dataset),
    new IndipendentAgent(env, 20, dataset),
    new IndipendentAgent(env, 21, dataset),
    new IndipendentAgent(env, 22, dataset),
    new IndipendentAgent(env, 23, dataset),
    new IndipendentAgent(env, 24, dataset)
  )

  new CTDESystem(agents, dataset, CCActions.toSeq(), env).learn(2000, 100)

}