package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model.*

class IndipendentAgent(environment: GeneralEnvironment, agentId: Int, dataset: ReplayBuffer[State, Action]):

    private var policy: State => Action = _

    def notifyNewPolicy(newPolicy: State => Action): Unit = policy = newPolicy
    def step: Unit =
        val state = environment.observe(agentId)
        val action = policy(state)
        val result: (Double, State) = environment.step(action, agentId)
        dataset.insert(state, action, result._1, result._2)