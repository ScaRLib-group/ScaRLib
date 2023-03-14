package it.unibo.scafi

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scarlib.core.model.State

abstract class ScafiProgram
  extends AggregateProgram
  with StandardSensors
  with ScafiAlchemistSupport
  {

  override def main(): Unit = {
    makeActions()
    node.put("state", computeState())
  }

  protected def computeState(): State

  protected def makeActions(): State

}
