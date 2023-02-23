package it.unibo.alchemist

import it.unibo.scarlib.core.model.{Action, RewardFunction, State}
import java.io.File
import it.unibo.alchemist.model.interfaces.{Actionable, Environment, Position, Time}
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule

class AlchemistEnvironment(envDefinition: String, rewardFunction: RewardFunction, actionSpace: Seq[Action]) {

  private val file = new File(envDefinition)
  private var engine = load()
  private def dt = 1.0 //TODO - settarlo con un senso

  def step(action: Action, agentId: Int): (Double, State) = {
    val actualState = observe(agentId)
    val node = engine.getEnvironment.getNodeByID(agentId)
    //TODO - node.put(action)
    incrementTime(dt)
    val newState = observe(agentId)
    val r = rewardFunction.compute(actualState, action, newState)
    (r, newState)
  }

  def observe(agentId: Int): State = {
    val state = engine.getEnvironment.getNodeByID(agentId).getConcentration(new SimpleMolecule("state")) //TODO - effettivamente come prendiamo lo state?
    ??? //TODO - Ritorna state
  }

  def reset: Unit = { engine = load() }

  private def load[P <: Position[P]](): Engine[Any, P] = {
    val env = LoadAlchemist.from(file).getDefault[Any, P]().getEnvironment
    new Engine(env)
  }

  private def incrementTime(dt: Double): Unit = {}

}
