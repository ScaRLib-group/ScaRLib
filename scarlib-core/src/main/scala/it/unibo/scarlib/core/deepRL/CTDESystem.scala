package it.unibo.scarlib.core.deepRL

import it.unibo.scarlib.core.model._
/*

case class CTDESystem(agents: Seq[IndipendentAgent], dataset: ReplayBuffer[State, Action], environment: GeneralEnvironment, learner: GeneralLearner):
  
  def start: Unit =
    agents.foreach(
      ag =>
        ag.setDataset(dataset)
        ag.setEnvironment(environment)
        ag.notifyNewPolicy(learner.getPolicy)
        ag.loop
    )

  def updatePolicy(policy: State => Action): Unit = agents.foreach(_.notifyNewPolicy(policy))
*/