package it.unibo.experiment.follow

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.experiment.cc.CCActions.{East, North, NorthEast, NorthWest, South, SouthEast, SouthWest, West}
import it.unibo.scafi.ScafiProgram
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.model.State

class FollowAgent
    extends ScafiProgram
    with FieldUtils
    with StandardSensors
    with BlockG
    with ScafiAlchemistSupport
    with CustomSpawn {
  private val size = 5
  private lazy val leaderId = sense[Int]("leaderId")
  private lazy val leader = sense[Int]("leaderId") == mid()
  override protected def computeState(): State = {
    node.put("isLeader", leader)
    val potentialToLeader = classicGradient(leader, nbrRange)
    val nearestToLeader = includingSelf.reifyField((nbr(potentialToLeader), nbrVector())).minBy(_._2._1)._2._2
    val distances = excludingSelf
      .reifyField(nbrVector())
      .toList
      .sortBy(_._2.distance(Point3D.Zero))
      .map(_._2)
      .map(point => (point.x, point.y))
      .take(size)
    branch(leader) {}(move())
    val agent = node.asInstanceOf[SimpleNodeManager[Any]].node
    new FollowAgentState(
      mid(),
      alchemistEnvironment.getDistanceBetweenNodes(agent, alchemistEnvironment.getNodeByID(leaderId)),
      distances,
      (nearestToLeader.x, nearestToLeader.y)
    )
  }

  def move(): Unit = {
    val dt = 0.05
    val agent = node.asInstanceOf[SimpleNodeManager[Any]].node
    val action = agent.getConcentration(new SimpleMolecule("action"))
    if (action != null) {
      val target = action match {
        //case NoAction => // do nothing
        case North =>
          alchemistEnvironment.getPosition(agent).plus(Array(0.0, dt))

        case South =>
          alchemistEnvironment.getPosition(agent).plus(Array(0.0, -dt))

        case West =>
          alchemistEnvironment.getPosition(agent).plus(Array(-dt, 0.0))

        case East =>
          alchemistEnvironment.getPosition(agent).plus(Array(dt, 0.0))

        case NorthEast =>
          alchemistEnvironment.getPosition(agent).plus(Array(dt, dt))

        case SouthEast =>
          alchemistEnvironment.getPosition(agent).plus(Array(dt, -dt))

        case NorthWest =>
          alchemistEnvironment.getPosition(agent).plus(Array(-dt, dt))

        case SouthWest =>
          alchemistEnvironment.getPosition(agent).plus(Array(-dt, -dt))
      }
      node.put("destination", target)
    }
  }
}
