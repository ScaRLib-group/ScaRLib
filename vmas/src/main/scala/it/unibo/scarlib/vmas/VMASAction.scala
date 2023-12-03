package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.Action
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.readwrite.Writer.floatWriter

abstract class VMASAction(tuple: (Float, Float)) extends Action{

    def toTensor(): py.Dynamic = {
        val np = py.module("numpy")
        val torch = py.module("torch")
        val array=np.array(Seq(tuple).toPythonCopy)
        torch.from_numpy(array)
    }

}

case object North extends VMASAction(tuple = (0.0f, 0.5f))
case object South extends VMASAction(tuple = (0.0f, -0.5f))

object VMASAction{
    def toSeq: Seq[VMASAction] = Seq(North, South)
}