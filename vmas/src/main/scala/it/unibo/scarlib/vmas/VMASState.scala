package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.State
import it.unibo.scarlib.core.neuralnetwork.NeuralNetworkEncoding
import it.unibo.scarlib.vmas
import me.shadaj.scalapy.py

object VMASState{
    def apply(array: py.Dynamic): VMASState = new VMASState(py.module("torch").tensor(array))
    private var stateDescriptor: Option[VMASStateDescriptor] = None
    def setDescriptor(descriptor: VMASStateDescriptor): Unit = stateDescriptor = Some(descriptor)

    implicit val encoding: NeuralNetworkEncoding[State] = new NeuralNetworkEncoding[State] {

        /** Gets the number of elements in the state */
        override def elements(): Int = stateDescriptor match {
            case Some(descriptor) => descriptor.getSize
            case None => throw new Exception("State descriptor not set")
        }

        /** Converts the state into a format usable by the neural network */
        override def toSeq(element: State): Seq[Double] = {
            val t1 = element.asInstanceOf[vmas.VMASState]
            val t2 = t1.tensor
            val t3 = t2.flatten().tolist()
            val t4 = t3.as[Seq[Double]]
            element.asInstanceOf[vmas.VMASState].tensor.flatten().tolist().as[Seq[Double]]
        }

    }

}

class VMASState(val tensor: py.Dynamic) extends State{

    /** Checks if the state is empty */
    override def isEmpty(): Boolean = false


}

