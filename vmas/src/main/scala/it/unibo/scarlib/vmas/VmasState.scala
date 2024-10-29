package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{AutodiffDevice, State}
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
import it.unibo.scarlib.vmas
import me.shadaj.scalapy.py

object VMASState{
    def apply(array: py.Dynamic): VMASState = new VMASState(py.module("torch").tensor(array).to(AutodiffDevice()))
    private var stateDescriptor: Option[VmasStateDescriptor] = None
    def setDescriptor(descriptor: VmasStateDescriptor): Unit = stateDescriptor = Some(descriptor)

    implicit val encoding: NeuralNetworkEncoding[State] = new NeuralNetworkEncoding[State] {

        /** Gets the number of elements in the state */
        override def elements(): Int = stateDescriptor match {
            case Some(descriptor) => descriptor.getSize
            case None => throw new Exception("State descriptor not set")
        }

        /** Converts the state into a format usable by the neural network */
        override def toSeq(element: State): Seq[Double] = element.asInstanceOf[vmas.VMASState].tensor.flatten().tolist().as[Seq[Double]]

    }

}

class VMASState(val tensor: py.Dynamic) extends State{

    /** Checks if the state is empty */
    override def isEmpty(): Boolean = false


}

