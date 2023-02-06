package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding


trait State:
    implicit val encoding: NeuralNetworkEncoding[State]

class TestState(distances: List[(Double, Double)], directionToLeader: (Double, Double)) extends State:
    override implicit val encoding: NeuralNetworkEncoding[State] = new NeuralNetworkEncoding[TestState] {
        override def elements: Int = (5 + 1) * 2

        override def toSeq(elem: TestState): Seq[Double] =
            elem.getDistances.flatMap { case (l, r) =>
                List(l, r)
            } ++ List(elem.getDirectionToLeader._1, elem.getDirectionToLeader._2)
    }.asInstanceOf[NeuralNetworkEncoding[State]]

    def getDistances: List[(Double, Double)] = distances

    def getDirectionToLeader: (Double, Double) = directionToLeader
