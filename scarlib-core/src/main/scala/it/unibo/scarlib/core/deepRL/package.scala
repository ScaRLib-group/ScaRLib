package it.unibo.scarlib.core

import scala.concurrent.ExecutionContext

package object deepRL:
  def async(any: => Unit)(using context: ExecutionContext): Unit =
    summon[ExecutionContext].execute(() => any)