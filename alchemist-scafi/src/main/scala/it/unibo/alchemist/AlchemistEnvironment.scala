package it.unibo.alchemist

import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.scarlib.core.model._

import java.io.File
import java.util.concurrent.TimeUnit
import _root_.scala.jdk.CollectionConverters._

class AlchemistEnvironment(
                            envDefinition: String,
                            rewardFunction: RewardFunction,
                            actionSpace: Seq[Action]
                          ) extends Environment(rewardFunction, actionSpace){

  private def dt = 1.0 
  private val file = new File(envDefinition)
  private val alchemistUtil = new AlchemistUtil()
  private var engine: Engine[Any, Nothing] = _
  this.reset()

  override def step(action: Action, agentId: Int): (Double, State) = {
    val actualState = observe(agentId)
    val node = engine.getEnvironment.getNodeByID(agentId)
    node.setConcentration(new SimpleMolecule("action"), action)
    engine.getEnvironment.getNodes.iterator().asScala.toList.filter(n => n.getId != agentId).foreach(n => n.setConcentration(new SimpleMolecule("action"), NoAction))
    alchemistUtil.incrementTime(dt, engine)
    val newState = observe(agentId)
    val r = rewardFunction.compute(actualState, action, newState)
    (r, newState)
  }

  override def observe(agentId: Int): State = {
      val state = engine.getEnvironment.getNodeByID(agentId).getConcentration(new SimpleMolecule("state"))
      if (state == null) {
          new EmptyState()
      } else {
          state.asInstanceOf[State]
      }
  }

  override def reset(): Unit = {
    if(engine != null) {
      engine.terminate()
      engine.waitFor(Status.TERMINATED, Long.MaxValue, TimeUnit.SECONDS)
    }
    engine = alchemistUtil.load(file)
  }

  override def log(): Unit = {}

  override def logOnFile(): Unit = {}

}