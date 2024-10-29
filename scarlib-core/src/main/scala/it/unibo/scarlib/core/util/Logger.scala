package it.unibo.scarlib.core.util

import me.shadaj.scalapy.py

trait Logger {
    def logScalar(tag: String, value: Double, tick: Int): Unit

    def logAny(tag: String, value: py.Dynamic, tick: Int): Unit
}
