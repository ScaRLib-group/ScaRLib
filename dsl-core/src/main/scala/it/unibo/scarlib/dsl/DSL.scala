package it.unibo.scarlib.dsl

import it.unibo.scarlib.core.deepRL.{CTDESystem, IndipendentAgent}
import it.unibo.scarlib.core.model.{Action, GeneralEnvironment, ReplayBuffer, RewardFunction, State}

import scala.reflect.runtime.universe as ru
import scala.collection.mutable
import scala.collection.mutable.Seq as MSeq

object DSL {

  private var rf: Option[RewardFunction] = Option.empty
  private var env: Option[GeneralEnvironment] = Option.empty
  private var ds: Option[ReplayBuffer[State, Action]] = Option.empty
  private var actionSpace: Seq[Action] = Seq.empty
  private var nAgents: Int = 0

  def learningSystem(init: Unit ?=> Unit): CTDESystem =
    given unit: Unit = ()
    init
    var agentsSeq: Seq[IndipendentAgent] = Seq.empty
    for (n <- 0 to nAgents) {
      agentsSeq = agentsSeq :+ new IndipendentAgent(env.get, n, ds.get, actionSpace)
    }
    new CTDESystem(agentsSeq, ds.get, actionSpace, env.get)

  def environment(init: Unit ?=> String) =
    given unit: Unit = ()
    val name: String = init
    val runtimeMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classSymbol = runtimeMirror.classSymbol(Class.forName(name))
    val classMirror = runtimeMirror.reflectClass(classSymbol)
    val constructor = classSymbol.typeSignature.members.filter(_.isConstructor).toList.head.asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor).apply(rf.get, actionSpace)
    env = Option(constructorMirror.asInstanceOf[GeneralEnvironment])

  def rewardFunction(init: Unit ?=> RewardFunction) =
    given unit: Unit = ()
    rf = Option(init)

  def actions(init: Unit ?=> Seq[Action]) =
    given unit: Unit = ()
    actionSpace = init

  def dataset(init: Unit ?=> ReplayBuffer[State, Action]) =
    given unit: Unit = ()
    ds = Option(init)

  def agents(init: Unit ?=> Int) =
    given unit: Unit = ()
    nAgents = init


  /*def actions(init: MSeq[Action] ?=> Unit)(using env: GeneralEnvironment)=
    given seq: MSeq[Action] = MSeq.empty
    init
    actionSpace = seq
    //env.setActions(set)*/

  /*def action(init: Unit ?=> Action)(using seq: MSeq[Action]) =
    given unit: Unit = ()
    seq :+ init*/


}



/*
import it.unibo.scarlib.core.model.{Action, Environment, RewardFunction}

import scala.collection.mutable.Set as MSet
import scala.collection.mutable.Map as MMap

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
  
  //def agent(agent: Class[Agent])(using env: Environment) =
  //  env.setAgent(agent)

  def agent(agent: String)(using env: Environment) =
    env.setAgent(agent)*/