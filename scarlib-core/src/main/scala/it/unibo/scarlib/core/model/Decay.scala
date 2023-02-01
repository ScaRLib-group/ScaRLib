package it.unibo.scarlib.core.model

trait Decay[T]:
  def update: Unit
  def value: T

class ExponentialDecay(initialValue: Double, rate: Double) extends Decay[Double]:

  private var elapsedTime: Int = 0

  override def update: Unit = elapsedTime = elapsedTime + 1
  override def value: Double = initialValue * Math.pow(1-rate, elapsedTime)