package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, Environment, RewardFunction, State}
import it.unibo.scarlib.core.neuralnetwork.TorchSupport
import it.unibo.scarlib.core.util.{AgentGlobalStore, Logger}

import scala.concurrent.Future
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class VmasEnvironment(rewardFunction: RewardFunction,
                      actionSpace: Seq[Action], settings: VmasSettings, logger: Logger, render: Boolean = false)
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

    //TODO Handling multiple environments
    private var lastObservation: List[Option[VMASState]] = List.fill(settings.nAgents)(None)
    private var steps = 0
    private var epochs = 0

    private var actions: Seq[py.Dynamic] = Seq[py.Dynamic]()
    private var futures = Seq[Future[(Double, State)]]()
    private var frames: py.Dynamic = py.Dynamic.global.list(Seq[py.Dynamic]().toPythonCopy)
    private val PIL = py.module("PIL")

    /** A single interaction with an agent
     *
     * @param action  the action performed by the agent
     * @param agentId the agent unique id
     * @return a [[Future]] that contains the reward of the action and the next state
     */
    override def step(action: Action, agentId: Int): Future[(Double, State)] = {
        //Check if agent is the last one
        val agents = env.agents.as[mutable.Seq[py.Dynamic]]
        //val agentPos = agents(agentId).pos //Tensor of shape [n_env, 2] - NOT USED
        actions = actions :+ action.asInstanceOf[VMASAction].toTensor()
        val nAgents:Int = env.n_agents.as[Int]
        val isLast = nAgents-1 == agentId
        val promise = scala.concurrent.Promise[(Double, State)]()
        val future = promise.future
        futures = futures :+ future
        if (isLast){
            steps += 1
            val result = env.step(actions.toPythonCopy)
            actions = Seq[py.Dynamic]()
            val observations = result.bracketAccess(0)
            val rewards = result.bracketAccess(1)
            for (i <- 0 until nAgents ) {
                val agentName = "agent_"+i
                val reward = rewards.bracketAccess(agentName).as[Double]
                val observation = observations.bracketAccess(agentName)
                val state = new VMASState(observation)
                lastObservation = lastObservation.updated(agentId, Some(state))  //TODO check if this is correct
                promise.success((reward, state))
                if (render) {
                    frames.append(
                        PIL.Image.fromarray(env.render(mode="rgb_array", agent_index_focus=py"None"))
                    )
                }
                if (steps == settings.nSteps) {
                    if(render) render(epochs)
                    epochs += 1
                    steps = 0
                }
            }
        }
        return future
    }

    /** Gets the current state of the environment */
    override def observe(agentId: Int): State = {
        lastObservation(agentId) match {
            case Some(state) => state
            case None => new VMASState(TorchSupport.deepLearningLib().from_numpy(TorchSupport.arrayModule.zeros(VMASState.encoding.elements())))
        }
    }

    /** Resets the environment to the initial state */
    override def reset(): Unit = {
        env.reset()
        lastObservation = List.fill(settings.nAgents)(None)
    }

    override def log(): Unit = {
        AgentGlobalStore.sumAllNumeric(AgentGlobalStore()).foreach { case (k, v) =>
            logger.logScalar(k, v, steps)
        }
        AgentGlobalStore().clearAll()
    }

    def render(epoch: Int) = {
        val gifName = settings.scenario.__class__.__name__.as[String] + "-" + epoch + ".gif"
        frames.bracketAccess(0).save(
            gifName,
            save_all = true,
            append_images = py"${frames}[1:]",
            duration = 1,
            loop = 0
        )
        frames = py.Dynamic.global.list(Seq[py.Dynamic]().toPythonCopy)
    }

    def logOnFile(): Unit = ???
}
