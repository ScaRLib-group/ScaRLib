package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.State
import me.shadaj.scalapy.py

object VMASState{
    def apply(array: py.Dynamic): VMASState = new VMASState(py.module("torch").tensor(array))
}

class VMASState(tensor: py.Dynamic) extends State{

    /** Gets the number of elements in the state */
    override def elements(): Int = tensor.numel().as[Int]

    /** Converts the state into a format usable by the neural network */
    override def toSeq(): Seq[Double] = tensor.flatten().as[Seq[Double]]

    /** Checks if the state is empty */
    override def isEmpty(): Boolean = elements() == 0
}
