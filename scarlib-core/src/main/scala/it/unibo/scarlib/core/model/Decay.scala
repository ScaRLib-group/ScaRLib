package it.unibo.scarlib.core.model

trait Decay[T]{
  def update(): Unit
  def value(): T
}


class ExponentialDecay(initialValue: Double, rate: Double, bound: Double) extends Decay[Double]{

  private var elapsedTime: Int = 0
  override def update(): Unit = elapsedTime = elapsedTime + 1
  override def value(): Double = {
    val v = initialValue * Math.pow(1 - rate, elapsedTime)
    if (v > bound) { v } else { bound }
  }
}

class ConstantDecay(initialValue: Double) extends Decay[Double]{
  override def update(): Unit = ()
  override def value(): Double = initialValue
}
