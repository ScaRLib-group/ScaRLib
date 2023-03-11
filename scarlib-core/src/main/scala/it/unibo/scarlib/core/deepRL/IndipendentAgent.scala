package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._

import scala.reflect.io.File
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class IndipendentAgent(
                        environment: Environment,
                        agentId: Int,
                        dataset: ReplayBuffer[State, Action],
                        val actionSpace: Seq[Action]
) {
  private var policy: State => Action = _
  private val posLogs: StringBuilder = new StringBuilder()
  private var oldState: State = _
  def notifyNewPolicy(newPolicy: State => Action): Unit =
    policy = newPolicy

  def step(): Future[Unit] = {
    val state = environment.observe(agentId)
    if (!state.isEmpty()) {
      val action = policy(state)
      environment
        .step(action, agentId)
        .map { result =>
          dataset.insert(state, action, result._1, result._2)
        }
        .map(_ => ())
      ///logPos(result._2.asInstanceOf[MyState].agentPosition, true)

    } else {
      environment.step(Random.shuffle(actionSpace).head, agentId)
      Future {}
    }
  }

  private def logPos(pos: (Double, Double), breakLine: Boolean = false): Unit = {
    posLogs.append(pos.toString)
    if (breakLine) {
      posLogs.append("\n")
    }
  }

  def logOnFile(): Unit = {
    val file = File(s"agent-$agentId.txt")
    val bw = file.bufferedWriter(append = true)
    bw.write(posLogs.toString())
    bw.close()
  }

}
