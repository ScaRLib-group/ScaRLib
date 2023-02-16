package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.MyState
import it.unibo.scarlib.core.model.*

import scala.reflect.io.File

class IndipendentAgent(environment: GeneralEnvironment, agentId: Int, dataset: ReplayBuffer[State, Action]):

    private var policy: State => Action = _
    private val posLogs: StringBuilder = StringBuilder()

    def notifyNewPolicy(newPolicy: State => Action): Unit = policy = newPolicy

    def step: Unit =
        val state = environment.observe(agentId)
        val action = policy(state)
        val result: (Double, State) = environment.step(action, agentId)
        logPos(result._2.asInstanceOf[MyState].agentPosition, true)
        dataset.insert(state, action, result._1, result._2)

    private def logPos(pos: (Double, Double), breakLine: Boolean = false): Unit =
        posLogs.append(pos.toString)
        if (breakLine) posLogs.append("\n")

    def logOnFile: Unit =
        val file = File(s"agent-${agentId}.txt")
        val bw = file.bufferedWriter(append = true)
        bw.write(posLogs.toString())
        bw.close()