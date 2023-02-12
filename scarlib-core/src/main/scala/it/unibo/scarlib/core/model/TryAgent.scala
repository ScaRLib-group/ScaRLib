package it.unibo.scarlib.core.model
import it.unibo.scarlib.core.neuralnetwork.DQN
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
import me.shadaj.scalapy.py
import scala.collection.immutable.Seq


class TryAgent:
  private var policy: State => Action = _
  private var state: State = _
  
  def updatePolicy(newPolicy: State => Action): Unit = policy = newPolicy //TODO - c'è un modo più intelligente di passarglielo?
  def act: Action = policy(state)
  def getState: State = state
  def updateState(s: State): Unit = state = s