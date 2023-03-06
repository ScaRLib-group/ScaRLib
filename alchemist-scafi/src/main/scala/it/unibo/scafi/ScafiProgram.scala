package it.unibo.scafi

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scarlib.core.model.State

abstract class ScafiProgram
  extends AggregateProgram
  with FieldUtils
  with StandardSensors
  with BlockG
  with ScafiAlchemistSupport
  with CustomSpawn {

  override def main(): Unit = { node.put("state", computeState()) }

  protected def computeState(): State

}