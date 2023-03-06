package it.unibo.alchemist

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.interfaces.{Actionable, Environment, Position, Time}

import java.io.File
import java.util.concurrent.{Executors, Semaphore}

class AlchemistUtil[P <: Position[P]]() {

  private var outputMonitor: Option[OutputMonitor[Any, P]] = Option.empty
  private var lock = new Semaphore(0)
    private val executor = Executors.newSingleThreadExecutor()

  def load(file: File): Engine[Any, P] = {
      val env = LoadAlchemist.from(file).getDefault[Any, P]().getEnvironment
      val eng = new Engine(env)
      this.lock = new Semaphore(0)
      outputMonitor = Option(timeToPause(1, lock, eng))
      eng.addOutputMonitor(outputMonitor.get)
      eng.play()
      executor.submit(new Runnable {
          override def run(): Unit = eng.run()
      })
      eng
  }

  def incrementTime(dt: Double, engine: Engine[Any, P]): Unit = {
    lock.acquire()
    val t = engine.getTime
    engine.removeOutputMonitor(outputMonitor.get)
    outputMonitor = Option(timeToPause(t.toDouble + dt, lock, engine))
    engine.addOutputMonitor(outputMonitor.get)
    engine.play()
  }

  private def timeToPause(stopWhen: Double, lock: Semaphore, engine: Engine[Any, P]): OutputMonitor[Any, P] = new OutputMonitor[Any, P]() {
    override def stepDone(
                           environment: Environment[Any, P],
                           reaction: Actionable[Any],
                           time: Time,
                           step: Long
                         ): Unit = {
      if (time.toDouble >= stopWhen) {
        lock.release()
        engine.pause()
      }
    }

    override def finished(environment: Environment[Any, P], time: Time, step: Long): Unit = {}

    override def initialized(environment: Environment[Any, P]): Unit = {}
  }

}
