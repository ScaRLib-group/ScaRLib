package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding


trait State:
    def elements: Int
    def toSeq(): Seq[Double]

class TestState(distances: List[(Double, Double)], directionToLeader: (Double, Double)) extends State:
    override def elements: Int = (5 + 1) * 2

    override def toSeq(): Seq[Double] =
        getDistances.flatMap { case (l, r) =>
            List(l, r)
        } ++ List(getDirectionToLeader._1, getDirectionToLeader._2)

    def getDistances: List[(Double, Double)] = distances

    def getDirectionToLeader: (Double, Double) = directionToLeader
