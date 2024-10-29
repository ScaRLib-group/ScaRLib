import math
import random
import typing
from typing import Dict, Callable, List

import torch
from torch import Tensor, tensor

from vmas import render_interactively
from vmas.simulator.core import Agent, Landmark, Sphere, World, Entity
from vmas.simulator.heuristic_policy import BaseHeuristicPolicy
from vmas.simulator.scenario import BaseScenario
from vmas.simulator.sensors import Lidar
from vmas.simulator.utils import Color, X, Y, ScenarioUtils

if typing.TYPE_CHECKING:
    from vmas.simulator.rendering import Geom


class Scenario(BaseScenario):
    def make_world(self, batch_dim: int, device: torch.device, **kwargs) -> World:
        self.n_agents = kwargs.get("n_agents", 5)
        self.target_distance = 5
        self._lidar_range = kwargs.get("lidar_range", 10)
        self.n_targets = kwargs.get("n_targets", 7)
        self._min_dist_between_entities = kwargs.get("min_dist_between_entities", 0.2)
        self._covering_range = kwargs.get("covering_range", 0.25)
        self._agents_per_target = kwargs.get("agents_per_target", 1)
        self.targets_respawn = kwargs.get("targets_respawn", True)
        self.shared_reward = kwargs.get("shared_reward", False)

        self.agent_collision_penalty = kwargs.get("agent_collision_penalty", 0)
        self.covering_rew_coeff = kwargs.get("covering_rew_coeff", 1.0)
        self.time_penalty = kwargs.get("time_penalty", 0)

        self._comms_range = self._lidar_range
        self.min_collision_distance = 0.005
        self.agent_radius = 0.05
        self.target_radius = self.agent_radius

        self.viewer_zoom = 1
        self.target_color = Color.GREEN

        # Make world
        world = World(
            batch_dim,
            device,
            x_semidim=1,
            y_semidim=1,
            collision_force=500,
            substeps=2,
            drag=0.25,
        )
        # Add agents
        entity_filter_agents: Callable[[Entity], bool] = lambda e: e.name.startswith(
            "agent"
        )
        for i in range(self.n_agents):
            # Constraint: all agents have same action range and multiplier
            agent = Agent(
                name=f"agent_{i}",
                collide=True,
                color=Color.RED,
                shape=Sphere(radius=self.agent_radius),
                sensors=[
                    Lidar(
                        world,
                        n_rays=50,
                        max_range=self._lidar_range,
                        entity_filter=entity_filter_agents,
                        render_color=Color.WHITE,
                    ),
                ],
            )
            world.add_agent(agent)

        return world

    def reset_world_at(self, env_index: int = None):
        entities = self.world.entities
        world = self.world
        x_bounds = (-world.x_semidim, world.x_semidim)
        y_bounds = (-world.y_semidim, world.y_semidim)
        for entity in entities:
            for j in range(world.batch_dim):
                import random
                pos = [random.uniform(x_bounds[0], x_bounds[1]), random.uniform(y_bounds[0], y_bounds[1])]
                entity.set_pos(tensor(pos), batch_index=j)



    def cohesionFactor(self, distances):
        max_distance = distances.max(dim=1, keepdim=True).values
        mask = (max_distance <= self.target_distance)
        max_distance[mask] = 0.0
        mask = (max_distance > self.target_distance)
        max_distance[mask] -= self.target_distance
        max_distance[mask] *= -1
        return max_distance

    def collisionFactor(self, distances):
        min_distance = distances.min(dim=1, keepdim=True).values
        mask = (min_distance > self.target_distance)
        min_distance[mask] = 0.0
        mask = (min_distance <= self.target_distance)
        min_distance[mask] /= self.target_distance
        min_distance[mask].log_()
        min_distance[mask] *= 2
        return min_distance

    def reward(self, agent: Agent):
        #return self.reward_pos(agent)
        return self.reward_lidar(agent)

    def reward_pos(self, agent: Agent):
        agents = self.world.agents
        obs = agent.sensors[0].measure()
        print(obs)
        print(obs.shape)
        agents_without_agent = list(filter(lambda x: x != agent, agents))
        agents_positions = torch.stack([t.state.pos for t in agents_without_agent], dim=1)
        diffs = agents_positions - agent.state.pos.unsqueeze(1)
        # Calculate Euclidean distance (L2 norm)
        distances = torch.norm(diffs, dim=-1)
        # Calculate cohesion factor
        cohesion_factor = self.cohesionFactor(distances)
        # Calculate collision factor
        collision_factor = self.collisionFactor(distances)
        return cohesion_factor + collision_factor

    def reward_lidar(self, agent: Agent):
        obs = agent.sensors[0].measure()
        min_distances = obs.min(dim=1, keepdim=True).values
        max_distances = obs.max(dim=1, keepdim=True).values
        mask = (min_distances > self.target_distance)
        min_distances[mask] = 0.0
        mask = (min_distances <= self.target_distance)
        min_distances[mask] /= self.target_distance
        min_distances[mask].log_()
        min_distances[mask] *= 2
        mask = (max_distances <= self.target_distance)
        max_distances[mask] = 0.0
        mask = (max_distances > self.target_distance)
        max_distances[mask] -= self.target_distance
        max_distances[mask] *= -1
        return min_distances + max_distances

    def observation(self, agent: Agent):
        lidar_1_measures = agent.sensors[0].measure()
        # lidar_2_measures = agent.sensors[1].measure()
        return torch.cat(
            [
                agent.state.pos,  # 2
                #agent.state.vel,  # 2
                # agent.state.pos, #15
                lidar_1_measures,
                # lidar_2_measures,
            ],
            dim=-1,
        )

    def info(self, agent: Agent) -> Dict[str, Tensor]:
        info = {
        }
        return info

    def done(self):
        return torch.zeros(self.world.batch_dim, dtype=torch.bool, device=self.world.device)

    def extra_render(self, env_index: int = 0) -> "List[Geom]":
        from vmas.simulator import rendering

        geoms: List[Geom] = []
        # Target ranges
        # Communication lines
        for i, agent1 in enumerate(self.world.agents):
            for j, agent2 in enumerate(self.world.agents):
                if j <= i:
                    continue
                agent_dist = torch.linalg.vector_norm(
                    agent1.state.pos - agent2.state.pos, dim=-1
                )
                if agent_dist[env_index] <= self._comms_range:
                    color = Color.BLACK.value
                    line = rendering.Line(
                        (agent1.state.pos[env_index]),
                        (agent2.state.pos[env_index]),
                        width=1,
                    )
                    xform = rendering.Transform()
                    line.add_attr(xform)
                    line.set_color(*color)
                    geoms.append(line)

        return geoms
