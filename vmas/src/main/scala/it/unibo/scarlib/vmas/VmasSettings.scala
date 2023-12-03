package it.unibo.scarlib.vmas

case class VmasSettings(
                         scenario: String,
                         nEnv: Int = 1,
                         nAgents: Int = 1,
                         nTargets: Int = 0,
                         nSteps: Int = 1000,
                         nEpochs: Int = 1,
                         device: String = "cpu",
                         continuousActions: Boolean = true,
                         dictionarySpaces: Boolean = true
                       )
