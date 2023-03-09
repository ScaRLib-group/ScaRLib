package it.unibo.experiment

import it.unibo.alchemist.AlchemistEnvironment
import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.{Action, CollectiveRewardFunction, ReplayBuffer, State}

object MySimulationExperiment extends App {

  private val rewardFunction = new CollectiveRewardFunction {
    override def compute(currentState: State, action: Action, newState: State): Double = {
      if (currentState.isEmpty() || currentState.asInstanceOf[MyState].positions.isEmpty) {
        0.0
      } else {
        val s = currentState.asInstanceOf[MyState]
        val d1 = euclideanDistance(s.agentPosition, s.positions.head)
        val d2 = euclideanDistance(s.agentPosition, s.positions.last)
        Math.abs(d1 - d2) * -1
      }
    }
  }

  private def euclideanDistance(x: (Double, Double), y: (Double, Double)): Double =
    Math.sqrt(Math.pow((x._1 - y._1), 2) + Math.pow((x._2 - y._2), 2))

  val env = new AlchemistEnvironment(
    "/home/gianluca/Programming/IdeaProjects/ScaRLib/alchemist-scafi/src/main/scala/it/unibo/experiment/simpleEnv.yaml",
    rewardFunction,
    Actions.toSeq()
  )

  val datasetSize = 10000
  private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)
  private val agents: Seq[IndipendentAgent] = Seq(
    new IndipendentAgent(env, 0, dataset, Actions.toSeq()),
    new IndipendentAgent(env, 1, dataset, Actions.toSeq()),
    new IndipendentAgent(env, 2, dataset, Actions.toSeq()),
    new IndipendentAgent(env, 3, dataset, Actions.toSeq())
  )
  new CTDESystem(agents, dataset, Actions.toSeq(), env).learn(1000, 50)
}

object Actions {
  case object North extends Action
  case object South extends Action
  case object East extends Action
  case object West extends Action

  def toSeq(): Seq[Action] = Seq(North, South, East, West)
}

case class MyState(positions: List[(Double, Double)], agentPosition: (Double, Double)) extends State {
  override def elements(): Int = 3 * 2

  override def toSeq(): Seq[Double] = {
    val fill = List.fill(elements())(0.0)
    (positions.flatMap { case (l, r) => List(l, r) } ++ fill).take(elements())
  }

  override def isEmpty(): Boolean = false
}