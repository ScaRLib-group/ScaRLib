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

    def __init__(self, rfLambda, obsLambda):
        self.rfLambda = rfLambda
        self.obsLambda = obsLambda

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

    def reward(self, agent):
        return self.rfLambda(self, agent)

    def observation(self, agent):
        return self.obsLambda(self, agent)


    def info(self, agent: Agent) -> Dict[str, Tensor]:
        info = {
        }
        return info

    def done(self):
        return torch.zeros(self.world.batch_dim, dtype=torch.bool, device=self.world.device)

    def extra_render(self, env_index: int = 0) -> "List[Geom]":
        geoms: List[Geom] = []
        return geoms
