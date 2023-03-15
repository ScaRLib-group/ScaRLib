package it.unibo.scarlib.core.model

trait RewardFunction {
  def compute(currentState: State, action: Action, newState: State): Double
}
