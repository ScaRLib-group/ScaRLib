package it.unibo.scarlib.core

import it.unibo.scarlib.core.deepRL.{DecentralizedAgent, DTDESystem}
import it.unibo.scarlib.core.model.{Action, Actuator, CollectiveRewardFunction, GeneralEnvironment, RewardFunction, State}

import scala.collection.mutable.Map

object Actions:
  case object North extends Action
  case object South extends Action
  case object Est extends Action
  case object West extends Action

  def toSeq: Seq[Action] = Seq(North, South, Est, West)

object MyActuator extends Actuator[Double]:

  private val dt: Double = 0.5

  override def convert(action: Action): Double = action match {
    case Actions.North => dt
    case Actions.South => -dt
    case Actions.Est => dt
    case Actions.West => -dt
  }

case class MyState(positions: List[(Double, Double)], agentPosition: (Double, Double)) extends State:
  override def elements: Int = 2 * 2
  override def toSeq(): Seq[Double] = positions.flatMap { case (l, r) => List(l, r) }

object TrySimulation extends App:

  private def euclideanDistance(x: (Double, Double), y: (Double, Double)): Double = Math.sqrt(Math.pow((x._1 - y._1), 2) + Math.pow((x._2 - y._2), 2))

  private val rewardFunction = new CollectiveRewardFunction {
    override def compute(currentState: State, newState: State): Double =
      val s = currentState.asInstanceOf[MyState]
      val d1 = euclideanDistance(s.agentPosition, s.positions.head)
      val d2 = euclideanDistance(s.agentPosition, s.positions.last)
      Math.abs(d1 - d2) * (-1)
  }

  private val actionSpace: Seq[Action] = Actions.toSeq


  private val environment = new GeneralEnvironment(rewardFunction, actionSpace) {
    private var positions: Map[Int, (Double, Double)] = Map((1, (3,5)), (2, (10,2)), (3, (1,18)))

    override def step(action: Action, agentId: Int): (Double, State) = //TODO - Synch
      this.synchronized(stepPreElaboration(action, agentId))
      //stepPreElaboration(action, agentId)

    private def stepPreElaboration(action: Action, agentId: Int): (Double, State) =
      val currentState = observe(agentId)
      val agentPos = positions.filter((index, pos) => index == agentId).values.head
      val newAgentPos: (Double, Double) = action match {
        case Actions.North => (agentPos._1 + MyActuator.convert(action), agentPos._2)
        case Actions.South => (agentPos._1 + MyActuator.convert(action), agentPos._2)
        case Actions.Est => (agentPos._1, agentPos._2 + MyActuator.convert(action))
        case Actions.West => (agentPos._1, agentPos._2 + MyActuator.convert(action))
      }
      positions.put(agentId, newAgentPos)
      val newState: State = MyState(positions.values.toList, newAgentPos)
      val reward: Double = rewardFunction.compute(currentState, newState)
      (reward, newState)

    override def observe(agentId: Int): State =
      println(s"Agent: ${agentId} --- inside observe")
      val otherPos = positions.filter((index, pos) => index != agentId).values.toList
      val myPos = positions.filter((index, pos) => index == agentId).values.head
      println(s"Agent: ${agentId} --- inside observe + after filter")
      MyState(positions.values.toList, myPos)

    //def setPositions(p: Map[Int, (Double, Double)]): Unit = positions = p

  }

  private val agents: Seq[DecentralizedAgent] = Seq(
    DecentralizedAgent(1, environment, 10000, actionSpace, -1, 1, 100),
    DecentralizedAgent(2, environment, 10000, actionSpace, -1, 1, 100),
    DecentralizedAgent(3, environment, 10000, actionSpace, -1, 1, 100)
  )

  DTDESystem(agents).start

  while true do ()
