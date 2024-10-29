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
import Device

if typing.TYPE_CHECKING:
    from vmas.simulator.rendering import Geom


class Scenario(BaseScenario):

    def __init__(self, f):
        f()

    def make_world(self, batch_dim: int, device: torch.device, **kwargs) -> World:
        self.n_agents = kwargs.get("n_agents", 5)
        self.neighbours = kwargs.get("neighbours", 5)
        self.device = kwargs.get("device", Device.get())
        self.agent_radius = 0.05
        self.viewer_zoom = 1
        self.target_distance = kwargs.get("target_distance", 5)
        self._comms_range = kwargs.get("comms_range", 0.35)

    # Make world
        world = World(
            batch_dim,
            device,
            x_semidim=5,
            y_semidim=5,
            collision_force=500,
            substeps=2,
            drag=0.25,
        )
        # Add agents
        for i in range(self.n_agents):
            # Constraint: all agents have same action range and multiplier
            agent = Agent(
                name=f"agent_{i}",
                collide=True,
                shape=Sphere(radius=self.agent_radius),
            )
            world.add_agent(agent)
        return world

    def reset_world_at(self, env_index: int = None):
        entities = self.world.entities
        world = self.world
        x_bounds = (-world.x_semidim-0.5, world.x_semidim-0.5)
        y_bounds = (-world.y_semidim-0.5, world.y_semidim-0.5)
        for entity in entities:
            for j in range(world.batch_dim):
                import random
                pos = [random.uniform(x_bounds[0], x_bounds[1]), random.uniform(y_bounds[0], y_bounds[1])]
                entity.set_pos(tensor(pos), batch_index=j)


    def cohesionFactor(self, distances):
        # max_distance = distances.max(dim=1, keepdim=True).values
        # mask = (max_distance > self.target_distance)
        # max_distance[mask] -= self.target_distance
        # max_distance[max_distance <= 0.0] = 0.0
        max_distance = distances[4]
        return -(max_distance - self.target_distance) if max_distance > self.target_distance else 0.0

    def collisionFactor(self, distances):
        # min_distance = distances.min(dim=1, keepdim=True).values
        # mask = (min_distance <= self.target_distance)
        # min_distance[mask] = 0.0
        # mask = (min_distance > 0.0)
        # min_distance[mask] /= self.target_distance
        # min_distance[mask].log_()
        # min_distance[min_distance <= 0.0] = 0.0
        # min_distance *= 2
        min_distance = distances[0]
        if min_distance == 0: return -100
        return 0 if min_distance >= self.target_distance else 2 * math.log(min_distance / self.target_distance)

    def reward(self, agent):
        agents = agent.obs
        if agents is None:
            return torch.zeros(self.world.batch_dim, dtype=torch.float32, device=self.world.device)
        agent_id = int(agent.name.split("_")[1])
        distances = self.distances[agent_id]

        cohesion_factor = self.cohesionFactor(distances)
        collision_factor = self.collisionFactor(distances)
        result = cohesion_factor + collision_factor
        return tensor(result, device=self.world.device).squeeze(0)

    def observation(self, agent):
        if agent.name == "agent_0":
            agents_positions = torch.stack([t.state.pos for t in self.world.agents], dim=1).squeeze(0).to(self.world.device)
            distances = torch.cdist(agents_positions, agents_positions)
            # Exclude the agent itself by setting diagonal elements to a large value
            distances.fill_diagonal_(float('inf'))
            # Sort distances along the second dimension (axis 1)
            sorted_distances, indices = torch.sort(distances, dim=1)
            # Select the top 5 indices for each agent
            self.distances = sorted_distances[:, :5]
            self.top_indices = indices[:, :5]
            self.top_positions = agents_positions[self.top_indices, :]
        agent_pos_to_device = agent.state.pos.to(self.world.device)
        agent_id = int(agent.name.split("_")[1])
        agent.obs = self.top_positions[agent_id]
        num_envs = self.world.batch_dim
        result = torch.cat([agent_pos_to_device.squeeze(0), agent.obs.view(10)], dim=-1)
        return result.unsqueeze(0)


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
        # for i, agent1 in enumerate(self.world.agents):
        #     for j, agent2 in enumerate(self.world.agents):
        #         if j <= i:
        #             continue
        #         agent_dist = torch.linalg.vector_norm(
        #             agent1.state.pos - agent2.state.pos, dim=-1
        #         )
        #         if agent_dist[env_index] <= self._comms_range:
        #             color = Color.BLACK.value
        #             line = rendering.Line(
        #                 (agent1.state.pos[env_index]),
        #                 (agent2.state.pos[env_index]),
        #                 width=1,
        #             )
        #             xform = rendering.Transform()
        #             line.add_attr(xform)
        #             line.set_color(*color)
        #             geoms.append(line)

        return geoms
