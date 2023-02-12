package it.unibo.scarlib.core.model

import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding

trait State:
    def elements: Int
    def toSeq(): Seq[Double]
