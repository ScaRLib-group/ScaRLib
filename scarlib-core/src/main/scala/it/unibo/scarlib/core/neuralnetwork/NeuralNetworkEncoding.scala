package it.unibo.scarlib.core.neuralnetwork

trait NeuralNetworkEncoding[A]{
    def elements: Int

    def toSeq(elem: A): Seq[Double]
}
