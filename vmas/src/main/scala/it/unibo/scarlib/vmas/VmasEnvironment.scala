package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model.{Action, AutodiffDevice, Environment, RewardFunction, State}
import it.unibo.scarlib.core.neuralnetwork.TorchSupport
import it.unibo.scarlib.core.util.{AgentGlobalStore, Logger}
import it.unibo.scarlib.vmas.WANDBLogger

import scala.concurrent.{ExecutionContext, Future}
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote
import me.shadaj.scalapy.py.SeqConverters

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import scala.collection.mutable

class VmasEnvironment(rewardFunction: RewardFunction,
                      actionSpace: Seq[Action])
  extends Environment(rewardFunction, actionSpace) {


    private var settings: VmasSettings = _
    private var logger: Logger = _
    private var render: Boolean = false
    private var framesFutures = List[Future[py.Dynamic]]()
    private val VMAS: py.Module = py.module("vmas")
    private var env: py.Dynamic = py.Dynamic.global.None

    def setSettings(settings: VmasSettings): Unit = {
        this.settings = settings
        lastObservation = List.fill(settings.nAgents)(None)
    }

    def setLogger(logger: Logger): Unit = this.logger = logger
    def enableRender(flag: Boolean): Unit = this.render = flag

    def initEnv(): Unit = env = makeEnv()

    private def makeEnv(): py.Dynamic = VMAS.make_env(
        scenario = settings.scenario,
        num_envs = settings.nEnv,
        device = settings.device,
        continuos_actions = settings.continuousActions,
        dict_spaces = settings.dictionarySpaces,
        n_agents = settings.nAgents,
        n_targets = settings.nTargets,
        neighbours = settings.neighbours
    )

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    //TODO Handling multiple environments
    private var lastObservation: List[Option[VMASState]] = List.empty
    private var steps = 0
    private var epochs = 0
    private val rendererExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10));

    private var actions: Seq[py.Dynamic] = Seq[py.Dynamic]()
    private var promises = Seq[scala.concurrent.Promise[(Double, State)]]()
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
        //val agentPos = agents(agentId).pos //Tensor of shape [n_env, 2] - NOT USED
        actions = actions :+ action.asInstanceOf[VMASAction].toTensor()
        val nAgents: Int = env.n_agents.as[Int]
        val isLast = nAgents - 1 == agentId
        val promise = scala.concurrent.Promise[(Double, State)]()
        val future = promise.future
        promises = promises :+ promise
        if (isLast) {
            steps += 1
            val result = env.step(actions.toPythonCopy)
            actions = Seq[py.Dynamic]()
            val observations = result.bracketAccess(0)
            val rewards = result.bracketAccess(1)
            for (i <- 0 until nAgents) {
                val agentName = "agent_" + i
                val reward = rewards.bracketAccess(agentName).as[Double]
                AgentGlobalStore().put(i, s"agent-$i-reward", reward)
                val observation = observations.bracketAccess(agentName)
                val state = new VMASState(observation)
                lastObservation = lastObservation.updated(agentId, Some(state)) //TODO check if this is correct
                promises(i).success((reward, state))
                if (render && steps % 25 == 0) {
                    framesFutures = framesFutures :+ appendFrame()
                }
                if (steps == settings.nSteps) {
                    val combinedFuture: Future[List[py.Dynamic]] = Future.sequence(framesFutures)
                    combinedFuture.onComplete(_ => ())
                    if (render) render(epochs)
                    epochs += 1
                    steps = 0
                    env = makeEnv()
                }
            }
            promises = Seq[scala.concurrent.Promise[(Double, State)]]()
        }
        return future
    }

    private def appendFrame(): Future[py.Dynamic] = Future(frames.append(
            PIL.Image.fromarray(env.render(mode = "rgb_array", agent_index_focus = py"None"))
        ))

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
        logger.asInstanceOf[WANDBLogger.type].log()
        AgentGlobalStore().clearAll()
    }

    def render(epoch: Int) = {
        // Get the current date and time
        val currentDateTime: LocalDateTime = LocalDateTime.now()
        // Define the desired date-time format
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")
        // Format the current date and time using the defined formatter
        val formattedDateTime: String = currentDateTime.format(formatter)
        val gifName = settings.scenario.__class__.__name__.as[String] + "-" + epoch + "-" + formattedDateTime + ".gif"
        print("Saving gif: " + gifName)
        val frames_copy = frames.copy()
        Future(frames_copy.bracketAccess(0).save(
            gifName,
            save_all = true,
            append_images = py"${frames_copy}[1:]",
            duration = 1,
            loop = 0
        ))(rendererExecutor).onComplete(_ => ())
        frames = py.Dynamic.global.list(Seq[py.Dynamic]().toPythonCopy)
    }

    def logOnFile(): Unit = ???
}
