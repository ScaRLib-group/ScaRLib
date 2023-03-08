package it.unibo.experiment.cc

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.experiment.cc.CCActions._
import it.unibo.scafi.ScafiProgram
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.model.State

class CCScafiAgent extends ScafiProgram {
  override protected def computeState(): State = {
    makeActions()
    val distances = excludingSelf
      .reifyField(nbrVector())
      .toList
      .sortBy(_._2.distance(Point3D.Zero))
      .map(_._2)
      .map(point => (point.x, point.y))
      .take(5)
    CCState(distances, mid())
  }

  private def makeActions(): Unit = {
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
