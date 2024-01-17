package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, AutodiffDevice}
import it.unibo.scarlib.core.neuralnetwork.TorchSupport
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import me.shadaj.scalapy.readwrite.Writer.floatWriter

abstract class VMASAction(tuple: (Float, Float)) extends Action{

    def toTensor(): py.Dynamic = {
        val np = TorchSupport.arrayModule
        val torch = TorchSupport.deepLearningLib()
        val array=np.array(Seq(tuple).toPythonCopy)
        torch.from_numpy(array).to(AutodiffDevice())
    }

}

case object North extends VMASAction(tuple = (0.0f, 1f * VMASAction.speed))
case object South extends VMASAction(tuple = (0.0f, -1f * VMASAction.speed))
case object East extends VMASAction(tuple = (1f * VMASAction.speed, 0.0f))
case object West extends VMASAction(tuple = (-1f * VMASAction.speed, 0.0f))
case object NorthEast extends VMASAction(tuple = (1f * VMASAction.speed, 1f * VMASAction.speed))
case object NorthWest extends VMASAction(tuple = (-1f * VMASAction.speed, 1f * VMASAction.speed))
case object SouthEast extends VMASAction(tuple = (1f * VMASAction.speed, -1f * VMASAction.speed))
case object SouthWest extends VMASAction(tuple = (-1f * VMASAction.speed, -1f * VMASAction.speed))

object VMASAction{
    val speed = 0.5f
    def toSeq: Seq[VMASAction] = Seq(North, South, East, West, NorthEast, NorthWest, SouthEast, SouthWest)
}