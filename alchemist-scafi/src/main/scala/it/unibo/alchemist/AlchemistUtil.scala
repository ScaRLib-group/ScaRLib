/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.alchemist

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.`export`.exporters.GlobalExporter
import it.unibo.alchemist.model.interfaces.{Actionable, Environment, Position, Time}
import java.io.File
import java.util.concurrent.{Executors, Semaphore}


class AlchemistUtil[P <: Position[P]]() {

  private var outputMonitor: Option[OutputMonitor[Any, P]] = Option.empty
  private var lock = new Semaphore(0)
  private val executor = Executors.newSingleThreadExecutor()

  def load(file: File, seed: Option[Int] = None): Engine[Any, P] = {
    val (env, exporters) = seed match {
      case Some(seed) =>
        val loader = LoadAlchemist.from(file).getWith[Any, P](java.util.Map.of("seed", seed)) // .getEnvironment
        val env = loader.getEnvironment
        val exporters = loader.getExporters
        val globalExporter = new GlobalExporter(exporters)
        (env, Some(globalExporter))
      case None => (LoadAlchemist.from(file).getDefault[Any, P]().getEnvironment, None)
    }
    val eng = new Engine(env)
    exporters.foreach(eng.addOutputMonitor(_))
    this.lock = new Semaphore(0)
    outputMonitor = Option(timeToPause(1, lock, eng)) // TODO - va bene così? Lo faccio partire ma lo pauso subito
    eng.addOutputMonitor(outputMonitor.get)
    eng.play()
    executor.submit(new Runnable {
      override def run(): Unit = eng.run()
    })
    lock.acquire()
    eng.removeOutputMonitor(outputMonitor.get)
    eng
  }

  def incrementTime(dt: Double, engine: Engine[Any, P]): Unit = {
    val t = engine.getTime
    outputMonitor.foreach(outputMonitor => engine.removeOutputMonitor(outputMonitor))
    engine.removeOutputMonitor(outputMonitor.get)
    outputMonitor = Option(timeToPause(t.toDouble + dt, lock, engine))
    engine.addOutputMonitor(outputMonitor.get)
    engine.play()
    lock.acquire()
    //engine.play()
  }

  private def timeToPause(stopWhen: Double, lock: Semaphore, engine: Engine[Any, P]): OutputMonitor[Any, P] =
    new OutputMonitor[Any, P]() {
      override def stepDone(
          environment: Environment[Any, P],
          reaction: Actionable[Any],
          time: Time,
          step: Long
      ): Unit = {
        if (time.toDouble >= stopWhen) {
          engine.pause()
          lock.release()
        }
      }

      override def finished(environment: Environment[Any, P], time: Time, step: Long): Unit = {}

      override def initialized(environment: Environment[Any, P]): Unit = {}
    }

}
