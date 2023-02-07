package it.unibo.scarlib.core.model

trait RewardFunction:
    def compute(states: Seq[State], agents: Seq[Agent], updates: Int): Double