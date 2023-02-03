package it.unibo.scarlib.core.model

import scala.util.Random

case class Experience[State, Action](actualState: State, action: Action, reward: Double, nextState: State)

trait ReplayBuffer[State, Action]:
  def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit
  def reset: Unit
  def subsample(batchSize: Int): Seq[Experience[State, Action]]

object ReplayBuffer:
  def apply[S, A](size: Int): ReplayBuffer[S, A] =
    new BoundedQueue[S, A](size)

  private class BoundedQueue[State, Action](size: Int) extends ReplayBuffer[State, Action]:

    private var queue: Seq[Experience[State, Action]] = Seq.empty

    override def reset: Unit = Seq.empty
    override def insert(actualState: State, action: Action, reward: Double, nextState: State): Unit =
      queue = (Experience(actualState, action, reward, nextState) +: queue).take(size)
    override def subsample(batchSize: Int): Seq[Experience[State, Action]] =
      Random(42).shuffle(queue).take(batchSize)

object BufferTest extends App:
  val buffer = ReplayBuffer[Double, Int](10)
  buffer.insert(1.0, 1, 10, 1.3)
  buffer.insert(1.3, 2, -1, 1.3)
  buffer.insert(1.3, 4, 2, 1.5)
  print(buffer.subsample(3))

object BufferTestMaxSize extends App:
  val buffer = ReplayBuffer[Double, Int](10)
  buffer.insert(1.0, 1, 10, 1.3)
  buffer.insert(1.1, 2, -1, 1.3)
  buffer.insert(1.2, 4, 2, 1.5)
  buffer.insert(1.3, 1, 10, 1.3)
  buffer.insert(1.4, 2, -1, 1.3)
  buffer.insert(1.5, 4, 2, 1.5)
  buffer.insert(1.6, 1, 10, 1.3)
  buffer.insert(1.7, 2, -1, 1.3)
  buffer.insert(1.8, 4, 2, 1.5)
  buffer.insert(1.9, 1, 10, 1.3)
  buffer.insert(1.19, 2, -1, 1.3)
  buffer.insert(1.11, 4, 2, 1.5)
  buffer.insert(1.12, 1, 10, 1.3)
  buffer.insert(1.13, 2, -1, 1.3)
  buffer.insert(1.14, 4, 2, 1.5)
  print(buffer.subsample(10)) //Should not contains 1.0, 1.1, 1.2 and 1.3