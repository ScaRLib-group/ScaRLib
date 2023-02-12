package it.unibo.scarlib.core.model

trait RewardFunction:
    def compute(currentState: State, newState: State): Double