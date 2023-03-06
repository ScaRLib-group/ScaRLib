package it.unibo.scarlib.core.model

trait Actuator[T]{
  def convert(action: Action): T
}
