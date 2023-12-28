package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.system.{CTDEAgent, DTDEAgent}
import it.unibo.scarlib.core.model.{Action, Experience, ReplayBuffer, State}
import it.unibo.scarlib.core.util.AgentGlobalStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

@deprecated("Use CTDEAgent instead")
object VmasCTDEAgent {
    private var INSTANCE_COUNTER = -1

    private def GET_AND_INCREMENT: Int = {
        INSTANCE_COUNTER += 1
        INSTANCE_COUNTER
    }
}

@deprecated("Use CTDEAgent instead")
class VmasCTDEAgent(environment: VmasEnvironment, 
                    actionSpace: Seq[VMASAction], 
                    dataset: ReplayBuffer[State, Action], 
                    agentId: Int = VmasCTDEAgent.GET_AND_INCREMENT)
  extends CTDEAgent(agentId = agentId,
      environment = environment,
      actionSpace = actionSpace,
      dataset = dataset
  ) {
    private var policy: State => Action = _

    override def step(): Future[Unit] = {
        val state = environment.observe(agentId)
        if (!state.isEmpty()) {
            val action = policy(state)
            environment
              .step(action, agentId)
              .map { result =>
                  dataset.insert(Experience(state, action, result._1, result._2))
              }
              .map(_ => ())

        } else {
            environment.step(Random.shuffle(actionSpace).head, agentId)
            Future {}
        }
    }

    override def notifyNewPolicy(newPolicy: State => Action): Unit =
        policy = newPolicy


}
