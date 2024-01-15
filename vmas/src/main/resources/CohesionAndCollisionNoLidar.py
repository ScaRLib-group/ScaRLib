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
    def make_world(self, batch_dim: int, device: torch.device, **kwargs) -> World:
        self.n_agents = kwargs.get("n_agents", 5)
        self.neighbours = kwargs.get("neighbours", 5)
        self.device = kwargs.get("device", Device.get())
        self.agent_radius = 0.01
        self.viewer_zoom = 1
        self.target_distance = kwargs.get("target_distance", 0.25)
        self._comms_range = kwargs.get("comms_range", 0.35)

    # Make world
        world = World(
            batch_dim,
            device,
            x_semidim=2,
            y_semidim=2,
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
        x_bounds = (-world.x_semidim/2, world.x_semidim/2)
        y_bounds = (-world.y_semidim/2, world.y_semidim/2)
        for entity in entities:
            for j in range(world.batch_dim):
                import random
                pos = [random.uniform(x_bounds[0], x_bounds[1]), random.uniform(y_bounds[0], y_bounds[1])]
                entity.set_pos(tensor(pos), batch_index=j)


    def cohesionFactor(self, distances):
        max_distance = torch.clamp(distances.max(dim=1, keepdim=True).values - self.target_distance, min=0.0)
        return -max_distance

    def collisionFactor(self, distances):
        min_distance = distances.min(dim=1, keepdim=True).values
        mask = min_distance <= self.target_distance
        min_distance[mask] = torch.where(mask, 2 * torch.log(min_distance[mask] / self.target_distance), torch.tensor(0.0, device=self.device))
        return min_distance

    def reward(self, agent):
        agents = agent.obs
        if agents is None:
            return torch.zeros(self.world.batch_dim, dtype=torch.float32, device=self.device)

        diffs = agents - agent.state.pos.unsqueeze(1).to(self.device)
        distances = torch.norm(diffs, dim=-1)

        cohesion_factor = self.cohesionFactor(distances)
        collision_factor = self.collisionFactor(distances)

        return cohesion_factor + collision_factor

    def observation(self, agent):
        agents = [a for a in self.world.agents if a != agent]
        agent_pos_to_device = agent.state.pos.to(self.device)
        agents_positions = torch.stack([t.state.pos for t in agents], dim=1).to(self.device)
        diffs = agents_positions - agent_pos_to_device.unsqueeze(1)
        distances = torch.norm(diffs, dim=-1)

        distances, indexes = distances.topk(self.neighbours, dim=1, largest=False)
        agents_positions = agents_positions.gather(dim=1, index=indexes.unsqueeze(-1).expand(-1, -1, 2))

        agent.obs = agents_positions
        num_envs = self.world.batch_dim

        return torch.cat([agent_pos_to_device.view(num_envs, 2), agents_positions.view(num_envs, -1)], dim=-1)


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
