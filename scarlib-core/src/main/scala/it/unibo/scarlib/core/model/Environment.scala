package it.unibo.scarlib.core.model

import scala.collection.mutable.{Map as MMap, Set as MSet}

class Environment:
  private var _rewardFunction: RewardFunction = _
  private var _actionSpace: Set[Action] = Set.empty
  private var _variables: Map[String, Double] = Map.empty
  //private var _agent: Class[Agent] = _
  private var _agent: String = _

  def setRewardFunction(rf: RewardFunction) = _rewardFunction = rf
  def setActions(set: MSet[Action]):Unit = _actionSpace = collection.immutable.Set[Action](set.toSeq:_*)
  def setVariables(map: MMap[String, Double]):Unit = _variables = map.toMap
  //def setAgent(a: Class[Agent]) = _agent = a
  def setAgent(a: String) = _agent = a

  def rewardFunction: RewardFunction = _rewardFunction
  def actionSpace: Set[Action] = _actionSpace
  def variables: Map[String, Double] = _variables
  def agent: String = _agent

  override def toString: String = s"rf: ${_rewardFunction}, as: ${_actionSpace}, vars: ${_variables}"




