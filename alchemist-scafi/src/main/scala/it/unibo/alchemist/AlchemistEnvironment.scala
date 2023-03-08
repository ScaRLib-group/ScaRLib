package it.unibo.alchemist

import it.unibo.alchemist.boundary.swingui.impl.{AlchemistSwingUI, SingleRunGUI, SingleRunSwingUI}
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.{Simulation, Status}
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Position, Position2D}
import it.unibo.scarlib.core.model._
import it.unibo.scarlib.core.util.{AgentGlobalStore, TorchLiveLogger}

import java.io.File
import java.util.concurrent.TimeUnit
import javax.swing.WindowConstants
import _root_.scala.jdk.CollectionConverters._

class AlchemistEnvironment(
    envDefinition: String,
    rewardFunction: RewardFunction,
    actionSpace: Seq[Action],
    outputStrategy: OutputStrategy = NoOutput
) extends GeneralEnvironment(rewardFunction, actionSpace) {

  private def dt = 1.0 // TODO - settarlo con un senso
  private val file = new File(envDefinition)
  private val alchemistUtil = new AlchemistUtil()
  private var engine: Engine[Any, Nothing] = _
  this.reset()
  private var agentIds = Set.empty[Int]
  private var ticks = 0
  override def step(action: Action, agentId: Int): (Double, State) = {
    if (agentIds.contains(agentId)) {
      engine.play()
      alchemistUtil.incrementTime(dt, engine)
      agentIds = Set.empty
      ticks += 1
    } else {
      agentIds += agentId
    }
    val actualState = observe(agentId)
    val node = engine.getEnvironment.getNodeByID(agentId)
    node.setConcentration(new SimpleMolecule("action"), action)
    /* engine.getEnvironment.getNodes
      .iterator()
      .asScala
      .toList
      .filter(n => n.getId != agentId)
      .foreach(n => n.setConcentration(new SimpleMolecule("action"), NoAction))*/
    val newState = observe(agentId)
    val r = rewardFunction.compute(actualState, action, newState)
    (r, newState)
  }

  override def observe(agentId: Int): State = {
    //    println(engine.getEnvironment.getSimulation.getTime)
    val state = engine.getEnvironment.getNodeByID(agentId).getConcentration(new SimpleMolecule("state"))
    if (state == null) {
      new EmptyState()
    } else {
      state.asInstanceOf[State]
    }
  }

  override def reset(): Unit = {
    if (engine != null) {
      engine.terminate()
      engine.waitFor(Status.TERMINATED, Long.MaxValue, TimeUnit.SECONDS)
    }
    engine = alchemistUtil.load(file)
    outputStrategy.output(engine)
  }

  override def log(): Unit = {
    AgentGlobalStore.averageAllNumeric(AgentGlobalStore()).foreach { case (k, v) =>
      TorchLiveLogger.logScalar(k, v, ticks)
    }
    AgentGlobalStore().clearAll()
  }

  override def logOnFile(): Unit =
    println("Log on file")

}

sealed trait OutputStrategy {
  def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit

  protected def render[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    val windows = java.awt.Window.getWindows
    windows.foreach(_.dispose())
    SingleRunGUI.make[T, P](simulation, WindowConstants.DO_NOTHING_ON_CLOSE)
  }
}
object NoOutput extends OutputStrategy {
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {}
}
class ShowEach(each: Int) extends OutputStrategy {
  private var episodes = 0
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    if (episodes % each == 0) {
      // get current awt window and close it
      render(simulation)
    }
    episodes += 1
  }
}
class After(ticks: Int) extends OutputStrategy {
  private var episodes = 0
  override def output[T, P <: Position2D[P]](simulation: Simulation[T, P]): Unit = {
    println(s"episodes: $episodes")
    if (ticks == episodes) {
      render(simulation)
    }
    episodes += 1
  }
}
