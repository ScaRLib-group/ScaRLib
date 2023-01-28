package it.unibo.scarlib.dsl

trait RewardFunction:
  def compute: Double

class SimpleRF extends RewardFunction:
  override def compute: Double = ???