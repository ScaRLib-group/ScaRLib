package it.unibo.scarlib.dsl

object AlchemistEnvironmentConverter:
  extension (env: Environment)
    def toAlchemistEnv: String =
      s"""
         |incarnation: scafi
         |variables:
         |  seed: {min: 0, max: 0, step: 1, default: 0}
         |  rewardFunction: &rewardFunction
         |    formula: ${"|"}
         |    ${env.rewardFunction}
         |    actionSpacE: ${env.actionSpace}
         |""".stripMargin
