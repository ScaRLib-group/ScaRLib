package it.unibo.scarlib.dsl

import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}


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

  def variables(init: MMap[String, Double] ?=> Unit )(using env: Environment) =
    given map: MMap[String, Double] = MMap.empty
    init
    env.setVariables(map)

  def variable(variableName: String, variableValue: Double)(using map: MMap[String, Double]) =
    map.addOne((variableName, variableValue))

  def v(variableName: String, variableValue: Double)(using map: MMap[String, Double]) = variable(variableName, variableValue)
  
  def agent(agent: Class[Agent])(using env: Environment) =
    env.setAgent(agent)