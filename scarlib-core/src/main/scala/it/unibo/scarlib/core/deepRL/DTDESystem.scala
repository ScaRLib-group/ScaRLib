package it.unibo.scarlib.core.deepRL

import scala.concurrent.ExecutionContext

class DTDESystem(agents: Seq[DecentralizedAgent]):
  
  def start: Unit =
    given context: ExecutionContext = ExecutionContext.global
    agents.foreach(
      ag =>
        async {
          ag.loop
        }
    )
    
    