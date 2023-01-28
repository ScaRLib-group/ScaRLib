package it.unibo.scarlib.dsl

implicit val myFactory: () => RewardFunction = () => new SimpleRF()

var env = environment{
  rewardFunction{}
}

