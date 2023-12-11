package it.unibo.alchemist

import it.unibo.alchemist.model.interfaces.{
  Action,
  Actionable,
  Condition,
  Dependency,
  Environment,
  GlobalReaction,
  Node,
  Position,
  Time,
  TimeDistribution
}
import org.danilopianini.util.{ListSet, ListSets}

import java.util
import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala

/** Represents a collective action performed after that each agent has decided the individual [[Action]] */
abstract class CollectiveAction[T, P <: Position[P]](
    val environment: Environment[T,P],
    distribution: TimeDistribution[T]
    ) extends GlobalReaction[T]{

  private val actions: util.List[Action[T]] = util.List.of()
  private val conditions: util.List[Condition[T]] = util.List.of()

  override def getActions: util.List[Action[T]] = actions

  override def setActions(list: util.List[_ <: Action[T]]): Unit = {
    actions.clear()
    actions.addAll(list)
  }

  override def setConditions(list: util.List[_ <: Condition[T]]): Unit = {
    conditions.clear()
    conditions.addAll(list)
  }

  override def execute(): Unit = {
    executeBeforeUpdateDistribution()
    distribution.update(getTimeDistribution.getNextOccurence, true, getRate, environment)
  }

  protected def executeBeforeUpdateDistribution(): Unit = {
    environment.getNodes.iterator().asScala.toList.foreach {
      node =>
        nodeAction(node)
    }
  }

  override def getConditions: util.List[Condition[T]] = conditions

  override def getInboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getOutboundDependencies: ListSet[_ <: Dependency] =  ListSets.emptyListSet()

  override def getTimeDistribution: TimeDistribution[T] = distribution

  override def canExecute: Boolean = true

  override def initializationComplete(time: Time, environment: Environment[T, _]): Unit = {}

  override def update(time: Time, b: Boolean, environment: Environment[T, _]): Unit = {}

  override def compareTo(o: Actionable[T]): Int = getTau.compareTo(o.getTau)

  override def getRate: Double = distribution.getRate

  override def getTau: Time = distribution.getNextOccurence

  protected def nodeAction(node: Node[T]): Unit
}
