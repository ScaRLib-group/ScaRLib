package it.unibo.scarlib.dsl

import scala.collection.mutable.{Set => MSet}

class Environment:
  private var rewardFunction: RewardFunction = _
  private var actionSpace: Set[Action] = Set.empty
  def setRewardFunction(rf: RewardFunction) = rewardFunction = rf
  def setActions(set: MSet[Action]):Unit = actionSpace = collection.immutable.Set[Action](set.toSeq:_*)

  override def toString: String = s"rf: ${rewardFunction}, as: ${actionSpace}"

def environment(init: Environment ?=> Unit) =
    given env: Environment = Environment()
    init
    env

def rewardFunction(init: Unit ?=> RewardFunction)(using env: Environment) =
  given unit: Unit = ()
  env.setRewardFunction(init)

def actions(init: MSet[Action] ?=> Unit)(using env: Environment) =
  given set: MSet[Action] = MSet.empty
  init
  env.setActions(set)

def action(a: Action)(using set: MSet[Action]) =
  set.add(a)