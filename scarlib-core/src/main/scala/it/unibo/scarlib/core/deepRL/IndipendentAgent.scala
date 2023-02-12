package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model.*
/*
class IndipendentAgent:

  private var policy: State => Action = _
  private var environment: GeneralEnvironment = _
  private var dataset: ReplayBuffer[State, Action] = _
  
  def setDataset(d: ReplayBuffer[State, Action]): Unit = dataset = d
  def setEnvironment(env: GeneralEnvironment): Unit = environment = env
  def notifyNewPolicy(newPolicy: State => Action): Unit = policy = newPolicy

  def loop: Unit =
      while(true){ //TODO - Trovare una condizione più sensata 
        val state = environment.observe
        val action = policy(state)
        val result: (Double, State) = environment.step(action)
        dataset.insert(state, action, result._1, result._2)
      }
*/