package it.unibo.experiment

import it.unibo.scafi.ScafiProgram
import it.unibo.scafi.space.Point3D
import it.unibo.scarlib.core.model.State

class ScafiAgent extends ScafiProgram {
    override protected def computeState(): State = {
        val distances = excludingSelf
          .reifyField(nbrVector())
          .toList
          .sortBy(_._2.distance(Point3D.Zero))
          .map(_._2)
          .map(point => (point.x, point.y))
          .take(3)
        MyState(distances, (currentPosition()._1, currentPosition()._2))
    }
}
