package it.unibo.scarlib.core

import it.unibo.scarlib.core.deepRL.{CTDESystem, DTDESystem, DecentralizedAgent, IndipendentAgent}
import it.unibo.scarlib.core.model.*

import scala.collection.mutable.Map
import scala.reflect.io.File

object Actions:
    case object North extends Action
    case object South extends Action
    case object Est extends Action
    case object West extends Action
    case object Clean extends Action

    def toSeq: Seq[Action] = Seq(North, South, Est, West, Clean)

object MyActuator extends Actuator[Double]:

    private val dt: Double = 1

    override def convert(action: Action): Double = action match {
        case Actions.North => dt
        case Actions.South => -dt
        case Actions.Est => dt
        case Actions.West => -dt
        case Actions.Clean => 0.0
    }

class MyEnv(rewardFunction: RewardFunction, actionSpace: Seq[Action]) extends GeneralEnvironment(rewardFunction, actionSpace) {
    private val _dustPositions = Seq.fill(5)((math.random * 200 - 100, math.random * 200 - 100)).toList
    private var positions: Map[Int, (Double, Double)] = Map((1, (3, 5)), (2, (10, 2)), (3, (1, 18)))
    private var dustPositions: List[(Double, Double)] = _dustPositions
    private var logs = new StringBuilder()

    override def step(action: Action, agentId: Int): (Double, State) =
        val currentState = observe(agentId)
        val agentPos = currentState.asInstanceOf[MyState].agentPosition
        val newAgentPos: (Double, Double) = action match {
            case Actions.North => (agentPos._1 + MyActuator.convert(action), agentPos._2)
            case Actions.South => (agentPos._1 + MyActuator.convert(action), agentPos._2)
            case Actions.Est => (agentPos._1, agentPos._2 + MyActuator.convert(action))
            case Actions.West => (agentPos._1, agentPos._2 + MyActuator.convert(action))
            case Actions.Clean => agentPos
        }
        if (action == Actions.Clean) then
            val cleanableDust = dustPositions.filter(euclideanDistance(_, newAgentPos) < 2.0)
            dustPositions = dustPositions.filter(!cleanableDust.contains(_))
            //dustPositions.filter(euclideanDistance(_, newAgentPos) < 2.0).foreach(dustPos => dustPositions = dustPositions.filter(_ != dustPos))
        positions.put(agentId, newAgentPos)
        val otherPos = positions.filter((index, pos) => index != agentId).values.toList
        val newState: State = MyState(otherPos, newAgentPos, dustPositions)
        val reward: Double = rewardFunction.compute(currentState, action, newState)
        (reward, newState)

    override def observe(agentId: Int): State =
        val otherPos = positions.filter((index, pos) => index != agentId).values.toList
        val myPos = positions.filter((index, pos) => index == agentId).values.head
        MyState(otherPos, myPos, dustPositions)

    override def reset: Unit =
        positions = Map((1, (3, 5)), (2, (10, 2)), (3, (1, 18)))
        dustPositions = _dustPositions //Seq.fill(20)((math.random * 150 - 75, math.random * 150 - 75)).toList

    override def log: Unit =
        dustPositions.foreach(dust => logs.append(s"$dust "))
        logs = logs.dropRight(1)
        logs.append("\n")

    override def logOnFile: Unit =
        val file = File("dusts.txt")
        val bw = file.bufferedWriter(false)
        bw.write(logs.toString.dropRight(1))
        bw.close()
}

case class MyState(positions: List[(Double, Double)], agentPosition: (Double, Double), dustsPositions: List[(Double, Double)]) extends State:
    override def elements: Int = 2 * 2

    override def toSeq(): Seq[Double] = positions.flatMap { case (l, r) => List(l, r) }

object TrySimulation extends App:

    /*private val rewardFunction = new CollectiveRewardFunction {
        override def compute(currentState: State, action: Action, newState: State): Double =
            val cs = currentState.asInstanceOf[MyState]
            val ns = newState.asInstanceOf[MyState]
            if (ns.dustsPositions.isEmpty) 100
            else if (cs.dustsPositions.length > ns.dustsPositions.length) 25
            else {
                val currentDistance = cs.dustsPositions.map(dust => euclideanDistance(dust, cs.agentPosition)).min
                val dustPos = cs.dustsPositions.filter(euclideanDistance(_, cs.agentPosition) == currentDistance).head
                //val newDistance = ns.dustsPositions.map(dust => euclideanDistance(dust, ns.agentPosition)).min //TODO use the dust pos used in the currentDistance or it might cause weird decisions
                val newDistance = euclideanDistance(dustPos, ns.agentPosition)
                val normalizedDistance = normalize(newDistance, 0, 100)
                if (newDistance < currentDistance)
                    10.0 * (1 - normalizedDistance)
                else
                    -10.0 * normalizedDistance
            }
    }*/

    private val rewardFunction = new CollectiveRewardFunction {
        override def compute(currentState: State, action: Action, newState: State): Double =
            val cs = currentState.asInstanceOf[MyState]
            var r: Double = 0.0
            if cs.dustsPositions.isEmpty then
              r = 100.0
            else
                r = action match {
                    case Actions.Clean =>
                        val cleanableDust = cs.dustsPositions.filter(euclideanDistance(cs.agentPosition, _) < 2.0)
                        if cleanableDust.isEmpty then -10.0 else 10.0
                    case _ => -1.0
                }
            r
    }

    private val actionSpace: Seq[Action] = Actions.toSeq
    private val environment = MyEnv(rewardFunction, actionSpace)


    //DTDE Simulation starter

    //  private val agents: Seq[DecentralizedAgent] = Seq(
    //    DecentralizedAgent(1, environment, 10000, actionSpace),
    //    DecentralizedAgent(2, environment, 10000, actionSpace),
    //    DecentralizedAgent(3, environment, 10000, actionSpace)
    //  )

    //DTDESystem(agents, environment).learn(5000, 100)


    //CTDE Simulation starter
    val datasetSize = 10000
    private val dataset: ReplayBuffer[State, Action] = ReplayBuffer[State, Action](datasetSize)

    private val agents: Seq[IndipendentAgent] = Seq(
        IndipendentAgent(environment, 1, dataset),
        IndipendentAgent(environment, 2, dataset),
        IndipendentAgent(environment, 3, dataset)
    )
    CTDESystem(agents, dataset, actionSpace, environment).learn(25, 300)

def euclideanDistance(x: (Double, Double), y: (Double, Double)): Double = Math.sqrt(Math.pow((x._1 - y._1), 2) + Math.pow((x._2 - y._2), 2))
def normalize(x: Double, min: Double, max: Double): Double = (x - min) / (max - min)