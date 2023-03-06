package it.unibo.experiment.cc

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.experiment.cc.CCActions.{East, North, NorthEast, NorthWest, South, SouthEast, SouthWest, West}
import it.unibo.scafi.ScafiProgram
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.model.{NoAction, State}

import _root_.scala.jdk.CollectionConverters._

class CCScafiAgent extends ScafiProgram {
  override protected def computeState(): State = {
    makeActions()
    val distances = excludingSelf
      .reifyField(nbrVector())
      .toList
      .sortBy(_._2.distance(Point3D.Zero))
      .map(_._2)
      .map(point => (point.x, point.y))
      .take(3)
    CCState(distances, mid())
  }

  private def makeActions(): Unit = {
    val dt = 0.05
    val agent = node.asInstanceOf[SimpleNodeManager[Any]].node
    val action = agent.getConcentration(new SimpleMolecule("action"))
    if (action != null) {
      action match {
        //case NoAction => // do nothing
        case North =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(0.0, dt))
          )
        case South =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(0.0, -dt))
          )
        case West =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(-dt, 0.0))
          )
        case East =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(dt, 0.0))
          )
        case NorthEast =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(dt, dt))
          )
        case SouthEast =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(dt, -dt))
          )
        case NorthWest =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(-dt, dt))
          )
        case SouthWest =>
          alchemistEnvironment.moveNodeToPosition(
            agent,
            alchemistEnvironment.getPosition(agent).plus(Array(-dt, -dt))
          )
      }
    }
  }

}
