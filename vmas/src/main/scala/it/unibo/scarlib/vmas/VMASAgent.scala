package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.deepRL.IndependentAgent
import it.unibo.scarlib.core.model.{Action, ReplayBuffer, State}

object VMASAgent {
    private var INSTANCE_COUNTER = 0

    private def GET_AND_INCREMENT: Int = {
        INSTANCE_COUNTER += 1
        INSTANCE_COUNTER
    }
}

class VMASAgent(environment: VmasEnvironment, actionSpace: Seq[VMASAction], dataset: ReplayBuffer[State, Action])
  extends IndependentAgent(agentId = VMASAgent.GET_AND_INCREMENT,
      environment = environment,
      actionSpace = actionSpace,
      dataset = dataset
  ) {




}
