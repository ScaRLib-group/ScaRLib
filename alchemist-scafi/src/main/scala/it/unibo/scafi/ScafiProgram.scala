package it.unibo.scafi

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

//TODO - Si ricava lo stato dell'agente e lo mette nella molecola state
trait ScafiProgram extends AggregateProgram
  with FieldUtils
  with StandardSensors
  with BlockG
  with ScafiAlchemistSupport
  with CustomSpawn 