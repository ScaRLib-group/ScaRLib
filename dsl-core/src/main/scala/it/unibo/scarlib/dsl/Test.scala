//package it.unibo.scarlib.dsl
//import DSL.*
//import AlchemistEnvironmentConverter.toAlchemistEnv
//import it.unibo.scarlib.core.model.SimpleRF
//import it.unibo.scarlib.core.model.Action
//
//object Test extends App {
//  var env = environment {
//    rewardFunction {
//      new SimpleRF()
//    }
//    actions {
//      action(new Action {
//        val name: String = "Prova1"
//        override def toString: String = name
//      })
//      action(new Action {
//        val name: String = "Prova2"
//        override def toString: String = name
//      })
//    }
//    variables {
//      variable("Var1", 2.0)
//      variable("Var2", 1.23)
//      v("Var3", 3.0)
//    }
//
//    agent("it.unibo.scarlib.core.model.SimpleAgent")
//    //agent(classOf[SimpleAgent])
//  }
//
//  print(env.toAlchemistEnv)
//}
