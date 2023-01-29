package it.unibo.scarlib.dsl

import scala.collection.mutable.{Set => MSet}

object DSL:
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