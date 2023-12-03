package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, Environment, RewardFunction, State}
import it.unibo.scarlib.core.util.{AgentGlobalStore, Logger}

import scala.concurrent.Future
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.collection.mutable

class VmasEnvironment(rewardFunction: RewardFunction,
                      actionSpace: Seq[Action], settings: VmasSettings, logger: Class[_ <: Logger])
  extends Environment(rewardFunction, actionSpace) {


    private val VMAS: py.Module = py.module("vmas")
    private val env:py.Dynamic = VMAS.make_env(
        scenario=settings.scenario,
        num_envs=settings.nEnv,
        device=settings.device,
        continuos_actions=settings.continuousActions,
        dict_spaces=settings.dictionarySpaces,
        n_agents=settings.nAgents,
        n_targets=settings.nTargets
    )

    private var lastObservation: Option[VMASState] = Option.empty
    private var ticks = 0

    /** A single interaction with an agent
     *
     * @param action  the action performed by the agent
     * @param agentId the agent unique id
     * @return a [[Future]] that contains the reward of the action and the next state
     */
    override def step(action: Action, agentId: Int): Future[(Double, State)] = {
        //Check if agent is the last one
        val agents = env.agents.as[mutable.Seq[py.Dynamic]]
        val agentPos = agents(agentId).pos //Tensor of shape [n_env, 2]
        
        val isLast = env.agents.len.as[Int]-1 == agentId
        if (isLast){

        }else{
        }
        return null
        //py""
    }

    /** Gets the current state of the environment */
    override def observe(agentId: Int): State = lastObservation match{
        case Some(obs) => obs
        case None => {
            lastObservation = Some(VMASState(env.get_observation_space()))
            lastObservation.get
        }
    }

    /** Resets the environment to the initial state */
    override def reset(): Unit = {
        env.reset()
        lastObservation = None
    }

    override def log(): Unit = {
        AgentGlobalStore.sumAllNumeric(AgentGlobalStore()).foreach { case (k, v) =>
            logger.logScalar(k, v, ticks)
        }
        AgentGlobalStore().clearAll()
    }

    override def logOnFile(): Unit = ???
}
