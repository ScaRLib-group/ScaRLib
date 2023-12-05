import math
import random
import typing
from typing import Dict, Callable, List

import torch

import Device
import wandb
from torch import Tensor, tensor

from vmas import render_interactively
from vmas.simulator.core import Agent, Landmark, Sphere, World, Entity
from vmas.simulator.heuristic_policy import BaseHeuristicPolicy
from vmas.simulator.scenario import BaseScenario
from vmas.simulator.sensors import Lidar
from vmas.simulator.utils import Color, X, Y, ScenarioUtils

if typing.TYPE_CHECKING:
    from vmas.simulator.rendering import Geom


def normalize(_value: torch.Tensor) -> torch.Tensor:
    _min = -100
    _max = 100
    return (_value - _min) / (_max - _min)


def normalize(_value: torch.Tensor, _min, _max) -> torch.Tensor:
    return (_value - _min) / (_max - _min)


class CleaningScenario(BaseScenario):
    def make_world(self, batch_dim: int, device: torch.device, **kwargs) -> World:
        self.wandb = kwargs.get("wandb", None)
        self.n_agents = kwargs.get("n_agents", 5)
        self.n_targets = kwargs.get("n_targets", 7)
        self.active_targets = torch.full((batch_dim, 1), self.n_targets, device=device)
        self.target_distance = kwargs.get("target_distance", 0.25)
        self._min_dist_between_entities = kwargs.get("min_dist_between_entities", 0.2)
        self._lidar_range = kwargs.get("lidar_range", 1.5)
        self.targets_pos = kwargs.get("targets_pos", [tensor([0, 0], device=Device.get())] * self.n_targets)
        self.n_targets_per_env = torch.full((batch_dim, 1), self.n_targets, device=device)

        self.min_collision_distance = 0.005
        self.agent_radius = 0.06
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
        entity_filter_targets: Callable[[Entity], bool] = lambda e: e.name.startswith(
            "target"
        )
        for i in range(self.n_agents):
            # Constraint: all agents have same action range and multiplier
            agent = Agent(
                name=f"agent_{i}",
                collide=True,
                shape=Sphere(radius=self.agent_radius),
                sensors=[
                    # Lidar(
                    #     world,
                    #     angle_start=0.05,
                    #     angle_end=2 * torch.pi + 0.05,
                    #     n_rays=50,
                    #     max_range=self._lidar_range,
                    #     entity_filter=entity_filter_agents,
                    #     render_color=Color.BLUE,
                    # ),
                    Lidar(
                        world,
                        n_rays=50,
                        max_range=self._lidar_range,
                        entity_filter=entity_filter_targets,
                        render_color=Color.GREEN,
                    ),
                ],
            )
            agent.prev_distances = torch.zeros((batch_dim, 1), device=device)
            world.add_agent(agent)

        self._targets = []
        for i in range(self.n_targets):
            target = Landmark(
                name=f"target_{i}",
                collide=True,
                movable=False,
                shape=Sphere(radius=self.target_radius),
                color=self.target_color,
            )
            world.add_landmark(target)
            self._targets.append(target)
            # for j in range(batch_dim):
            #     target.set_pos(self.targets_pos[i], j)

        return world

    def reset_world_at(self, env_index: int = None):
        placable_entities = self._targets[: self.n_targets] + self.world.agents
        ScenarioUtils.spawn_entities_randomly(
            entities=placable_entities,
            world=self.world,
            env_index=env_index,
            min_dist_between_entities=self._min_dist_between_entities,
            x_bounds=(-self.world.x_semidim, self.world.x_semidim),
            y_bounds=(-self.world.y_semidim, self.world.y_semidim),
        )
        # for target in self._targets[self.n_targets:]:
        #     target.set_pos(self.get_outside_pos(env_index), batch_index=env_index)
        # for i, target in enumerate(self._targets):
        #     for j in range(self.world.batch_dim):
        #         target.set_pos(self.targets_pos[i], j)

    def respawn_targets(self, agent: Agent):
        targets = self._targets
        targets_positions = torch.stack([t.state.pos for t in targets], dim=0).to(Device.get())
        distances = []
        expanded_agent_pos = agent.state.pos.unsqueeze(0)
        distance = torch.norm(targets_positions - expanded_agent_pos, dim=-1).unsqueeze(-1).to(Device.get())# - self.agent_radius - self.target_radius
        for (i, target) in enumerate(targets):
            for j in range(self.world.batch_dim):
                if distance[i][j] <= self.target_distance:
                    target.set_pos(torch.tensor([-10000, -10000]).to(Device.get()), batch_index=j)
                    self.active_targets[j] -= 1
                    # bool_check = self.active_targets.flatten() == 0
                    # if bool_check.all(True):
                    #     self.done()
                    #     return

    def random_pos(self):
        x_coord = torch.full((self.world.batch_dim, 1), random.uniform(-self.world.x_semidim, self.world.x_semidim))
        y_coord = torch.full((self.world.batch_dim, 1), random.uniform(-self.world.y_semidim, self.world.y_semidim))
        tensor = torch.cat((x_coord, y_coord), dim=1)
        return tensor

    def reward(self, agent: Agent):
        # return self.reward_pos(agent)
        return self.reward_lidar(agent)

    def reward_lidar(self, agent: Agent):
        k = 1
        targets_lidar = agent.sensors[0].measure()
        #agents_lidar = agent.sensors[0].measure()
        # get distances from nearest target
        # targets_positions = torch.stack([t.state.pos for t in self._targets], dim=0).to(Device.get())
        # min_distances_from_targets = (torch.norm(targets_positions - agent.state.pos.unsqueeze(0), dim=-1).unsqueeze(-1)
        #                               .to(Device.get()))
        # t = min_distances_from_targets.min(dim=0).values.float().squeeze(0).view(self.world.batch_dim, 1)
        min_distances_from_lidar = targets_lidar.min(dim=1, keepdim=True).values.float()
        final_rewards = min_distances_from_lidar.clone().to(Device.get())
        mask = min_distances_from_lidar >= self._lidar_range
        if mask.any():
            #rewards_for_agents_outside_lidar_range = -torch.exp(-t[mask]).to(Device.get())
            final_rewards[mask] = -1 #-(2/math.pi) * (torch.atan(k * t[mask]).to(Device.get())) #rewards_for_agents_outside_lidar_range  # -(t[~mask]**2)
        mask = min_distances_from_lidar <= self.target_distance
        if mask.any():
            final_rewards[mask] = torch.full([self.world.batch_dim, 1], self.n_targets, device=Device.get(), dtype=torch.float32) - (self.active_targets) + 1
        mask = (min_distances_from_lidar > self.target_distance) & (min_distances_from_lidar < self._lidar_range) & (min_distances_from_lidar < agent.prev_distances)
        if mask.any():
                final_rewards[mask] = -0.5 + ((-min_distances_from_lidar[mask] / self._lidar_range + 1) * -0.5 / 1) #1 - (min_distances_from_lidar[mask] / self._lidar_range)
        mask = (min_distances_from_lidar > self.target_distance) & (min_distances_from_lidar < self._lidar_range) & (min_distances_from_lidar >= agent.prev_distances)
        if mask.any():
            final_rewards[mask] = -1 + ((-min_distances_from_lidar[mask] / self._lidar_range + 1) * -0.5)
        #rewards_for_agents_inside_lidar_range = -torch.exp(-t[mask]).to(Device.get())/2#torch.sigmoid(temp[~new_mask]).to(Device.get())
            #final_rewards[mask] = -(2/math.pi) * (torch.atan(k * t[mask]).to(Device.get()))#-torch.exp(-t[mask]).to(Device.get())/2 #rewards_for_agents_inside_lidar_range
            #final_rewards[mask] = -1 + (t[mask].clone().to(Device.get()) - self._lidar_range) * (1 - 0) / (0 - self._lidar_range)

        # reward for agents
        # min_distances = agents_lidar.min(dim=1, keepdim=True).values.float()
        # mask = min_distances < self._lidar_range
        # temp = min_distances.clone()
        # temp[~mask] = -self._lidar_range/3.0
        # temp[~mask] /= min_distances[~mask]
        # min_distances[~mask] = temp[~mask]
        # temp = min_distances.float().clone()
        # temp[mask] = -self._lidar_range/3.0
        # temp[mask] /= min_distances[mask]
        # agents_reward = temp

        self.respawn_targets(agent)
        # print(agent.name)
        # print(targets_reward)
        # print(agents_reward)
        # print(final_reward)
        for i in range(self.world.batch_dim):
            if self.active_targets[i] == 0:
                final_rewards[i] = 10
        agent.prev_distances = min_distances_from_lidar
        return final_rewards

    def get_outside_pos(self, env_index):
        return torch.empty(
            (1, self.world.dim_p)
            if env_index is not None
            else (self.world.batch_dim, self.world.dim_p),
            device=self.world.device,
        ).uniform_(-1000 * self.world.x_semidim, -1000 * self.world.x_semidim)

    def agent_reward(self, agent):
        agent_index = self.world.agents.index(agent)

        agent.covering_reward[:] = 0
        targets_covered_by_agent = (
                self.agents_targets_dists[:, agent_index] < self._covering_range
        )
        num_covered_targets_covered_by_agent = (
                targets_covered_by_agent * self.covered_targets
        ).sum(dim=-1)
        agent.covering_reward += (
                num_covered_targets_covered_by_agent * self.covering_rew_coeff
        )
        return agent.covering_reward

    def observation(self, agent: Agent):
        lidar_1_measures = agent.sensors[0].measure()
        #lidar_2_measures = agent.sensors[1].measure()
        return torch.cat(
            [
                #agent.state.pos,  # 2
                #agent.state.vel,  # 2
                lidar_1_measures,  # 15
                #lidar_2_measures,  # 15
            ],
            dim=-1,
        ).to(Device.get())

    def info(self, agent: Agent) -> Dict[str, Tensor]:
        info = {
            "active_targets": self.active_targets
        }
        return info

    def done(self):
        return torch.zeros((self.world.batch_dim, 1), device=self.world.device)

    def extra_render(self, env_index: int = 0) -> "List[Geom]":
        from vmas.simulator import rendering

        geoms: List[Geom] = []
        # Target ranges
        for i, target in enumerate(self._targets):
            range_circle = rendering.make_circle(self.target_distance, filled=False)
            xform = rendering.Transform()
            xform.set_translation(*target.state.pos[env_index])
            range_circle.add_attr(xform)
            range_circle.set_color(*self.target_color.value)
            geoms.append(range_circle)
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
