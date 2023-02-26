package it.unibo.alchemist

import it.unibo.alchemist.model.interfaces._
import it.unibo.scarlib.core.model.Action

abstract class ActuatorReaction[T, P <: Position[P]](
    environment: Environment[T, P],
    distribution: TimeDistribution[T],
    actionSpace: Seq[Action]
   ) extends AbstractReaction[T,P](environment, distribution){

  override def executeBeforeUpdateDistribution(): Unit = { makeAction() }

  protected def makeAction(): Unit
}
