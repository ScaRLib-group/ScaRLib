package it.unibo.scarlib.dsl

import scala.concurrent.ExecutionContext.Implicits.global
import it.unibo.scarlib.core.model.*
import it.unibo.scarlib.dsl.DSL.*

import scala.concurrent.Future

class TestEnv(rewardFunction: RewardFunction, actionSpace: Seq[Action])
    extends Environment(rewardFunction, actionSpace) {

  override def step(action: Action, agentId: Int): Future[(Double, State)] = ???

  override def observe(agentId: Int): State = ???

  override def reset(): Unit = ???

  override def log(): Unit = ???

  override def logOnFile(): Unit = ???
}

class TestRF extends RewardFunction {
  override def compute(currentState: State, action: Action, newState: State): Double = ???
}

object TestActions {
  case object North extends Action

  case object South extends Action

  def toSeq: Seq[Action] = Seq(North, South)
}

object Test extends App {

  val system = learningSystem {

    rewardFunction {
      new TestRF()
    }

    actions {
      TestActions.toSeq
    }

    dataset {
      ReplayBuffer[State, Action](10000)
    }

    agents {
      50
    }

    environment {
      "it.unibo.scarlib.dsl.TestEnv"
    }

  }

  println(system)

  system.learn(1000, 100)
}

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
