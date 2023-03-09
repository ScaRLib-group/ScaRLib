package it.unibo.scarlib.core.model

abstract class Environment(rewardFunction: RewardFunction, actionSpace: Seq[Action]){
    def step(action: Action, agentId: Int): (Double, State)

    def observe(agentId: Int): State

    def reset(): Unit

    def log(): Unit

    def logOnFile(): Unit
}
