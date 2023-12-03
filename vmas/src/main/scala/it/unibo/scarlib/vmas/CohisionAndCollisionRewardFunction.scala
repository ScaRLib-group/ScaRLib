package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, RewardFunction, State}

class CohisionAndCollisionRewardFunction extends RewardFunction{
    override def compute(currentState: State, action: Action, newState: State): Double = ???
}
