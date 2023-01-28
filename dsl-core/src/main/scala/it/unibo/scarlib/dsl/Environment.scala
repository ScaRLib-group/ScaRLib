package it.unibo.scarlib.dsl
class Environment:
  private var rewardFunction: RewardFunction = _
  private var actionSpace: ActionSpace = _
  def setRewardFunction(rf: RewardFunction) = rewardFunction = rf
  
def environment(init: Environment ?=> Unit) =
    given env: Environment = Environment()
    init
    env

def rewardFunction(init: RewardFunction ?=> Unit)(using env: Environment)(using factory: () => RewardFunction) =
  given rf: RewardFunction = factory()
  init
  env.setRewardFunction(rf)