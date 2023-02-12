package it.unibo.scarlib.core.model

trait RewardFunction:
    def compute(states: Seq[State], updates: Int): Double