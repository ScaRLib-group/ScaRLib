package it.unibo.scarlib.dsl

import it.unibo.scarlib.core.model.Environment

object AlchemistEnvironmentConverter:
  extension (env: Environment)
    def toAlchemistEnv: String =
      var yml = s"""
                   |incarnation: scafi
                   |variables:
                   |  seed: {min: 0, max: 0, step: 1, default: 0}
                   |""".stripMargin

      env.variables.foreach((n,v) =>
        yml = yml +
          s"""  ${n}: &${n} { formula: ${v} }
            |""".stripMargin
      )
      yml = yml +
        s"""  rewardFunction: &rewardFunction
            |   formula: ${"|"}
            |     import ???
            |     new ${env.rewardFunction}
            |    languade: scala
            |  actionSpace: &actionSapce { formula: ${env.actionSpace} , language: scala }
            |
            |environment:
            | type: Continuos2DEnvironment
            | parameters: []
            | global-programs:
            |   - time-distribution: 1
            |     type: CentralLearner
            |     parameters: [???????????]
            |
            |network-model:
            | type: ClosestN
            | paramteres: [5]
            |
            |_reactions:
            | - program: &program
            |   - time-distribution:
            |     type: DiracComb
            |     parameters: [1.0]
            |   type: Event
            |   actions:
            |     - type: RunScafiProgram
            |       parameters: [${env.agent}, 1.1]
            |   - program: send  
            |
            |deployments:
            | type: Grid
            | parameters: [-5, -5, 5, 5, 2, 2, 1, 1]
            | programs:
            |   - *program
            |""".stripMargin
      yml

