package it.unibo.experiment

import it.unibo.alchemist.ActuatorReaction
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Environment, Position, TimeDistribution}
import it.unibo.experiment.Actions.{East, North, South, West}
import it.unibo.scarlib.core.model.{Action, NoAction}

class Actuator[T, P <: Position[P]](env: Environment[T, P], distribution: TimeDistribution[T], actionSpace: Seq[Action]) extends ActuatorReaction(env, distribution, actionSpace) {

    private val dt = 0.5

    override protected def makeAction(): Unit = {
        agents.foreach { agent =>
            val action = agent.getConcentration(new SimpleMolecule("action")).asInstanceOf[Action]
            action match {
                case NoAction => // do nothing
                case North => env.moveNodeToPosition(agent, env.getPosition(agent).plus(Array(0.0, dt)))
                case South => env.moveNodeToPosition(agent, env.getPosition(agent).plus(Array(0.0, -dt)))
                case West => env.moveNodeToPosition(agent, env.getPosition(agent).plus(Array(-dt, 0.0)))
                case East => env.moveNodeToPosition(agent, env.getPosition(agent).plus(Array(dt, 0.0)))
            }
        }
    }
}
