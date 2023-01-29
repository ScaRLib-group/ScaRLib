package it.unibo.scarlib.dsl

sealed trait RewardFunction:
  def compute: Double

trait CollectiveRewardFunction extends RewardFunction

trait LocalRewardFunction extends RewardFunction

class SimpleRF extends CollectiveRewardFunction:
  override def compute: Double = ???