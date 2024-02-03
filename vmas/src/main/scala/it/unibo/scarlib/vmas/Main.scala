package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.model._
import it.unibo.scarlib.dsl.DSL._
import it.unibo.scarlib.vmas.RewardFunctionDSL.{AddOps, AddRoot, AddTwoStepOps, CurrentState, Lambda, MapOps, NewState, ReduceOps, Tensor, doubleToPyDynamic, rewardFunctionStep, tensorToPyDynamic}
import me.shadaj.scalapy.interpreter.CPythonInterpreter
import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.PyQuote

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

object Main extends App {
    //    val nNeighbour = 5
    //    private val memory = ReplayBuffer[VMASState, VMASAction](10000)
    private val actions = VMASAction.toSeq
    val nAgents = 200
    val nSteps = 100
    val t = Lambda("x: x")

    val test = AddRoot(10.0, CurrentState) ++ AddRoot(-5.5, NewState) + Tensor(5) --> Lambda("x: x") >> Lambda("x: x.min()")
    println(test.toString)
    rewardFunctionStep {
        AddRoot(10.0, CurrentState) ++ AddRoot(-5.5, NewState) + Tensor(5) --> Lambda("x: x") >> Lambda("x: x.min()")
    }

    CPythonInterpreter.execManyLines(
        """def rf(env, agent):
            import torch
            import math
            agents = agent.obs
            if agents is None:
                return torch.zeros(env.world.batch_dim, dtype=torch.float32, device=env.world.device)
            agent_id = int(agent.name.split("_")[1])
            distances = env.distances[agent_id]
            max_distance = distances[4]
            cohesion_factor = -(max_distance - env.target_distance) if max_distance > env.target_distance else 0.0
            min_distance = distances[0]
            collision_factor = 0
            if min_distance == 0:
                collision_factor = -100
            else:
                collision_factor =  0 if min_distance >= env.target_distance else 2 * math.log(min_distance / env.target_distance)
            result = cohesion_factor + collision_factor
            return torch.tensor(result, device=env.world.device).squeeze(0)
          """
    )
    val rfLambda = py.Dynamic.global.rf
    CPythonInterpreter.execManyLines(
        """def obs(env, agent):
            import torch
            if agent.name == "agent_0":
                agents_positions = torch.stack([t.state.pos for t in env.world.agents], dim=1).squeeze(0).to(env.world.device)
                distances = torch.cdist(agents_positions, agents_positions)
                # Exclude the agent itself by setting diagonal elements to a large value
                distances.fill_diagonal_(float('inf'))
                # Sort distances along the second dimension (axis 1)
                sorted_distances, indices = torch.sort(distances, dim=1)
                # Select the top 5 indices for each agent
                env.distances = sorted_distances[:, :5]
                env.top_indices = indices[:, :5]
                env.top_positions = agents_positions[env.top_indices, :]
            agent_pos_to_device = agent.state.pos.to(env.world.device)
            agent_id = int(agent.name.split("_")[1])
            agent.obs = env.top_positions[agent_id]
            num_envs = env.world.batch_dim
            result = torch.cat([agent_pos_to_device.squeeze(0), agent.obs.view(10)], dim=-1)
            return result.unsqueeze(0)
    """)
    val obsLambda = py.Dynamic.global.obs
    WANDBLogger.init()
    private val nNeighbour = 5
    val stateDescriptor = VmasStateDescriptor(hasPosition = true, hasVelocity = false, extraDimension = nNeighbour * 2)
    VMASState.setDescriptor(stateDescriptor)
    val scenario = py.module("AbstractEnv").Scenario(rfLambda, obsLambda)
    private val envSettings = VmasSettings(scenario = scenario, nEnv = 1, nAgents = nAgents, nTargets = 0,
        nSteps = nSteps, nEpochs = 150, device = "cpu", neighbours = nNeighbour)
    implicit val configuration: Environment => Unit = (e: Environment) => {
        val env = e.asInstanceOf[VmasEnvironment]
        env.setSettings(envSettings)
        env.setLogger(WANDBLogger)
        env.enableRender(false)
        env.initEnv()
    }
    private val where = s"./networks"
    val system = CTDELearningSystem {
        rewardFunction {
            EmptyRewardFunction()
        }
        actionSpace {
            VMASAction.toSeq
        }
        dataset {
            ReplayBuffer[State, Action](10000)
        }
        agents {
            200
        }
        learningConfiguration {
            LearningConfiguration(dqnFactory = new NNFactory(stateDescriptor, VMASAction.toSeq), snapshotPath = where)
        }
        environment {
            "it.unibo.scarlib.vmas.VmasEnvironment"
        }
    }(ExecutionContext.global, VMASState.encoding)
    system.learn(envSettings.nEpochs, envSettings.nSteps)
}

case class EmptyRewardFunction() extends RewardFunction {
    override def compute(currentState: State, action: Action, newState: State): Double = 0.0
}
