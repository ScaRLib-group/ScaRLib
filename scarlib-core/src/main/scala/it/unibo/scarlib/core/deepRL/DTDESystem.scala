package it.unibo.scarlib.core.deepRL

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class DTDESystem(agents: Seq[DecentralizedAgent]):

  @tailrec
  final def learn(episodes: Int): Unit =
    if episodes > 0 then
      agents.foreach(_.step)
      learn(episodes-1)

    /*given context: ExecutionContext = ExecutionContext.global
    agents.foreach(
      ag =>
        async {
          ag.loop
        }
    )*/

    