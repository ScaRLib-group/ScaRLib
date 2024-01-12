package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.AutodiffDevice
import me.shadaj.scalapy.py

case class VmasSettings(
                         scenario: py.Dynamic,
                         nEnv: Int = 1,
                         nAgents: Int = 1,
                         nTargets: Int = 0,
                         nSteps: Int = 1000,
                         nEpochs: Int = 1,
                         device: String = AutodiffDevice().as[String],
                         continuousActions: Boolean = true,
                         dictionarySpaces: Boolean = true,
                         neighbours: Int = 0
                       )
