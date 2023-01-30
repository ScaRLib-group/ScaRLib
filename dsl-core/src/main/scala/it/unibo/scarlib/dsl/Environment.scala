package it.unibo.scarlib.dsl

import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}

class Environment:
  private var _rewardFunction: RewardFunction = _
  private var _actionSpace: Set[Action] = Set.empty
  private var _variables: Map[String, Double] = Map.empty
  private var _agent: Class[Agent] = _

  def setRewardFunction(rf: RewardFunction) = _rewardFunction = rf
  def setActions(set: MSet[Action]):Unit = _actionSpace = collection.immutable.Set[Action](set.toSeq:_*)
  def setVariables(map: MMap[String, Double]):Unit = _variables = map.toMap
  def setAgent(a: Class[Agent]) = _agent = a

  def rewardFunction: RewardFunction = _rewardFunction
  def actionSpace: Set[Action] = _actionSpace
  def variables: Map[String, Double] = _variables

  override def toString: String = s"rf: ${_rewardFunction}, as: ${_actionSpace}, vars: ${_variables}"




