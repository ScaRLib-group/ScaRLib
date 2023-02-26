package it.unibo.experiment

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb
import it.unibo.experiment.Actions.{East, North, South, West}
import it.unibo.scafi.ScafiProgram
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.model.{Action, NoAction, State}

import _root_.scala.jdk.CollectionConverters._

class ScafiAgent extends ScafiProgram {
    override protected def computeState(): State = {
        makeActions()
        val distances = excludingSelf
          .reifyField(nbrVector())
          .toList
          .sortBy(_._2.distance(Point3D.Zero))
          .map(_._2)
          .map(point => (point.x, point.y))
          .take(3)
        MyState(distances, (currentPosition()._1, currentPosition()._2))
    }

    private def makeActions(): Unit = {
      val dt = 1
      alchemistEnvironment.getNodes.iterator().asScala.toList.foreach(
        agent => {
            val action = agent.getConcentration(new SimpleMolecule("action"))
            action match {
                case NoAction => // do nothing
                case North => alchemistEnvironment.moveNodeToPosition(agent, alchemistEnvironment.getPosition(agent).plus(Array(0.0, dt)))
                case South => alchemistEnvironment.moveNodeToPosition(agent, alchemistEnvironment.getPosition(agent).plus(Array(0.0, -dt)))
                case West => alchemistEnvironment.moveNodeToPosition(agent, alchemistEnvironment.getPosition(agent).plus(Array(-dt, 0.0)))
                case East => alchemistEnvironment.moveNodeToPosition(agent, alchemistEnvironment.getPosition(agent).plus(Array(dt, 0.0)))
            }
        }
      )
    }
}
