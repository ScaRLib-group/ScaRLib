package it.unibo.scarlib.core.util

import it.unibo.scarlib.core.neuralnetwork.TorchSupport
import me.shadaj.scalapy.py

object TorchLiveLogger {
    private val writer = TorchSupport.logger().SummaryWriter()

    def logScalar(tag: String, value: Double, tick: Int): Unit = writer.add_scalar(tag, value, tick)

    def logAny(tag: String, value: py.Dynamic, tick: Int): Unit = writer.add_scalar(tag, value, tick)

}

