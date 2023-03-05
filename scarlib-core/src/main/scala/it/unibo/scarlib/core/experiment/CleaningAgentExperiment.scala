package it.unibo.scarlib.core.experiment

import it.unibo.scarlib.core.model.{Action, Actuator, State}


object CAActions {
  case object North extends Action
  case object South extends Action
  case object East extends Action
  case object West extends Action
  case object Clean extends Action

  def toSeq() = Seq(North, South, East, West, Clean)

}

object CAActuator extends Actuator[Double] {

  import CAActions._

  private val dt = 1

  override def convert(action: Action): Double = {
    action match {
      case North => dt
      case South => -dt
      case East => dt
      case West => -dt
      case Clean => 0.0
    }
  }
}

case class CAState(
                    positions: List[(Double, Double)],
                    selfPosition: (Double, Double),
                    dustsPosition: List[(Double, Double)]) extends State {

  override def elements(): Int = ???

  override def toSeq(): Seq[Double] = ???

  override def isEmpty(): Boolean = false

}

class CleaningAgentExperiment {

}
