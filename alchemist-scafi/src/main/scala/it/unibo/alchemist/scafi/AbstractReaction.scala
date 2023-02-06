package it.unibo.alchemist.scafi

import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces._
import org.danilopianini.util.{ListSet, ListSets}
import java.util
import scala.jdk.CollectionConverters.IteratorHasAsScala

abstract class AbstractReaction[T, P <: Position[P]](
    val environment: Environment[T, P],
    distribution: TimeDistribution[T]) extends GlobalReaction[T]{

  private val actions: util.List[Action[T]] = util.List.of()
  private val conditions: util.List[Condition[T]] = util.List.of()

  override def getActions: util.List[Action[T]] = actions
  override def setActions(list: util.List[_ <: Action[T]]): Unit = {
    actions.clear()
    actions.addAll(list)
  }

  override def getConditions: util.List[Condition[T]] = conditions
  override def setConditions(list: util.List[_ <: Condition[T]]): Unit = {
    conditions.clear()
    conditions.addAll(list)
  }

  protected def executeBeforeUpdateDistribution(): Unit

  override def getTimeDistribution: TimeDistribution[T] = distribution

  override def getRate: Double = distribution.getRate

  override def execute(): Unit = {
    executeBeforeUpdateDistribution()
    distribution.update(getTimeDistribution.getNextOccurence, true, getRate, environment)
  }

  override def getInboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getOutboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def canExecute: Boolean = true

  override def initializationComplete(time: Time, environment: Environment[T, _]): Unit = {}

  override def update(time: Time, b: Boolean, environment: Environment[T, _]): Unit = {}

  override def getTau: Time = distribution.getNextOccurence

  override def compareTo(other: Actionable[T]): Int = getTau.compareTo(other.getTau)

  def agents = environment.getNodes.iterator().asScala.toList

  def managers = agents.map(new SimpleNodeManager[T](_))

}