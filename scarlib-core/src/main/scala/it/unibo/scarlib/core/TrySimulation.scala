package it.unibo.scarlib.core

import it.unibo.scarlib.core.deepRL.{CTDESystem, DTDESystem, DecentralizedAgent, IndipendentAgent}
import it.unibo.scarlib.core.model.*
import it.unibo.scarlib.core.util.TorchLiveLogger

import javax.swing.{JFrame, JPanel, WindowConstants}
import scala.collection.mutable
import scala.collection.mutable.Map
import scala.reflect.io.File
import scala.util.Random

object Actions:
  case object North extends Action
  case object South extends Action
  case object Est extends Action
  case object West extends Action
  case object Clean extends Action

  def toSeq: Seq[Action] = Seq(North, South, Est, West, Clean)

object MyActuator extends Actuator[Int]:

  private val dt: Int = 1

  override def convert(action: Action): Int = action match {
    case Actions.North => dt
    case Actions.South => -dt
    case Actions.Est => dt
    case Actions.West => -dt
    case Actions.Clean => 0
  }

private class EnvView(var worldSize: Int) extends JFrame {
  worldSize = worldSize + 1
  // create a panel to draw on.
  val panel = new JPanel()
  // put the layer as grid, with sizexsize dimension
  panel.setLayout(new java.awt.GridLayout(worldSize, worldSize))
  // put size x size button in the panel
  for (i <- 0 until worldSize * worldSize) {
    val button = new javax.swing.JButton()
    button.setBackground(java.awt.Color.WHITE)
    panel.add(button)
  }
  // add the panel to the frame
  this.add(panel)
  // set the size of the frame
  this.setSize(800, 600)
  // set the default close operation (exit when it gets closed)
  this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  // make the frame visible
  this.setVisible(true)

  def renderEnvironment(agents: List[(Int, Int)], dust: List[(Int, Int)]): Unit = {
    // reset buttons
    for (i <- 0 until worldSize * worldSize) {
      val button = panel.getComponent(i).asInstanceOf[javax.swing.JButton]
      button.setText(s"")
      button.setBackground(java.awt.Color.WHITE)
    }
    // render the agents from the list
    agents.foreach { case (x, y) =>
      val button = panel.getComponent(x * worldSize + y).asInstanceOf[javax.swing.JButton]
      button.setText(s"$x, $y")
      button.setBackground(java.awt.Color.RED)
    }
    // render the dust from the list
    dust.foreach { case (x, y) =>
      val button = panel.getComponent(x * worldSize + y).asInstanceOf[javax.swing.JButton]
      button.setText(s"$x, $y")
      button.setBackground(java.awt.Color.BLACK)
    }
  }
}

class MyEnv(
    rewardFunction: RewardFunction,
    actionSpace: Seq[Action],
    dusts: Int,
    val bound: Int = 5,
    val seed: Int = 42
) extends GeneralEnvironment(rewardFunction, actionSpace) {
  private var historyOfPositions: List[List[(Int, Int)]] = List.empty
  private var historyOfDust: List[List[(Int, Int)]] = List.empty
  private val view = new EnvView(bound)
  private val random = new Random(seed)
  private val allPossibleDust = (0 until bound).flatMap(x => (0 until bound).map(y => (x, y)))
  private val _dustPositions = random.shuffle(allPossibleDust).take(dusts).toList
  private var positions: mutable.Map[Int, (Int, Int)] = mutable.Map((1, (3, 5)), (2, (1, 3)), (3, (3, 3)))
  private var dustPositions: List[(Int, Int)] = _dustPositions
  private var logs = new StringBuilder()
  private var step: Int = 0
  private var resets: Int = 0
  override def step(action: Action, agentId: Int): (Double, State) =
    val currentState = observe(agentId)
    val agentPos = currentState.asInstanceOf[MyState].agentPosition
    val newAgentPos: (Int, Int) = action match {
      case Actions.North => (wrap(agentPos._1 + MyActuator.convert(action)), agentPos._2)
      case Actions.South => (wrap(agentPos._1 + MyActuator.convert(action)), agentPos._2)
      case Actions.Est => (agentPos._1, wrap(agentPos._2 + MyActuator.convert(action)))
      case Actions.West => (agentPos._1, wrap(agentPos._2 + MyActuator.convert(action)))
      case Actions.Clean => agentPos
    }
    if Actions.Clean == action then dustPositions = dustPositions.filterNot(_ == newAgentPos)
    positions.put(agentId, newAgentPos)
    val otherPos = positions.filter((index, pos) => index != agentId).values.toList
    val newState: State = MyState(otherPos, newAgentPos, dustPositions, bound = bound)
    val reward: Double = rewardFunction.compute(currentState, action, newState)
    TorchLiveLogger.logScalar(s"Reward-agent-$agentId", reward, step)
    step += 1
    historyOfDust = dustPositions :: historyOfDust
    historyOfPositions = positions.values.toList :: historyOfPositions
    (reward, newState)

  override def observe(agentId: Int): State =
    val otherPos = positions.filter((index, pos) => index != agentId).values.toList
    val myPos = positions.filter((index, pos) => index == agentId).values.head
    MyState(otherPos, myPos, dustPositions)

  override def reset: Unit =
    resets += 1
    /*if(resets % 2 == 0) {
          historyOfDust.reverse.zip(historyOfPositions.reverse).foreach {
              case (dust, agents) =>
                  view.renderEnvironment(agents, dust)
                  Thread.sleep(33)
          }
        }*/
    historyOfPositions = List.empty
    historyOfPositions = List.empty
    positions = mutable.Map((1, (3, 5)), (2, (1, 3)), (3, (3, 3)))
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

  private def wrap(number: Int): Int =
    (number % bound + bound) % bound
}

case class MyState(
    positions: List[(Int, Int)],
    agentPosition: (Int, Int),
    dustsPositions: List[(Int, Int)],
    totalDust: Int = 5,
    bound: Double = 5.0
) extends State:
  override def elements: Int = (positions.length + totalDust) * 2

  override def toSeq(): Seq[Double] =
    positions
      .flatMap { case (l, r) => List(l, r) }
      .appendedAll(dustsPositions.flatMap { case (l, r) => List(l, r) })
      .appendedAll(Seq.fill((totalDust - dustsPositions.length) * 2)(-1))
      .map(_.toDouble / bound)

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
      val ns = newState.asInstanceOf[MyState]
      var r: Double = 0.0

      r = action match {
        case Actions.Clean =>
          if (cs.dustsPositions.length > ns.dustsPositions.length)
            -ns.dustsPositions.size
          else
            -10
        case _ => -ns.dustsPositions.size
      }

      -ns.dustsPositions.size
  }

  private val actionSpace: Seq[Action] = Actions.toSeq
  private val environment = MyEnv(rewardFunction, actionSpace, 5)

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
  CTDESystem(agents, dataset, actionSpace, environment).learn(2000, 300)

def euclideanDistance(x: (Double, Double), y: (Double, Double)): Double =
  Math.sqrt(Math.pow((x._1 - y._1), 2) + Math.pow((x._2 - y._2), 2))
def normalize(x: Double, min: Double, max: Double): Double = (x - min) / (max - min)
