package it.unibo.scarlib.dsl

import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}

class Environment:
  private var rewardFunction: RewardFunction = _
  private var actionSpace: Set[Action] = Set.empty
  private var variables: Map[String, Double] = Map.empty
  private var agent: Class[Agent] = _

  def setRewardFunction(rf: RewardFunction) = rewardFunction = rf
  def setActions(set: MSet[Action]):Unit = actionSpace = collection.immutable.Set[Action](set.toSeq:_*)
  def setVariables(map: MMap[String, Double]):Unit = variables = map.toMap
  def setAgent(a: Class[Agent]) = agent = a

  override def toString: String = s"rf: ${rewardFunction}, as: ${actionSpace}, vars: ${variables}"
