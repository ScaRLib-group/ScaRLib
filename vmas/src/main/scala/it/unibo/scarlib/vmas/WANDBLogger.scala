package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.util.Logger
import me.shadaj.scalapy.py

object WANDBLogger extends Logger {

    private val wandb = py.module("wandb")

    private var wandbInit: Option[py.Dynamic] = None

    private var map: py.Dynamic = py.Dynamic.global.dict()

    def login(): Unit = wandb.login()

    def init(): py.Dynamic = {
        wandbInit = Some(wandb.init(
            project = "vmas",
            entity = "scarlib"
        ))
        wandbInit.get
    }

    override def logScalar(tag: String, value: Double, tick: Int): Unit = {
        map.bracketUpdate(tag, value)
    }

    override def logAny(tag: String, value: py.Dynamic, tick: Int): Unit = {
        map.bracketUpdate(tag, value)
    }

    def log(): Unit = {
        wandbInit match {
            case Some(w) => w.log(map)
            case None =>
        }
    }
}
