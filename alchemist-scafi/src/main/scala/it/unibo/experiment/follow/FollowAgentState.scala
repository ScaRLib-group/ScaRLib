package it.unibo.experiment.follow

import it.unibo.scarlib.core.model.State

class FollowAgentState(
    val id: Int,
    val distanceToLeader: Double, // only visible during training
    val distances: List[(Double, Double)],
    val toLeader: (Double, Double)
) extends State {
  private val encodingSize = 2
  override def elements(): Int = 5 * encodingSize + encodingSize

  override def toSeq(): Seq[Double] = (distances.flatMap { case (l, r) =>
    List(l, r)
  } ++ List(toLeader._1, toLeader._2) ++ List.fill(10)(0.0)).take(elements())

  override def isEmpty(): Boolean = distances.isEmpty
}
