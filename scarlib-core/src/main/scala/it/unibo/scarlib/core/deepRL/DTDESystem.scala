/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * GNU General Public License as described in the file LICENSE in the ScaRLin distribution's top directory.
 */

package it.unibo.scarlib.core.deepRL

import scala.annotation.tailrec
import it.unibo.scarlib.core.model.{Environment, PolicyNN}
import scala.concurrent.{ExecutionContext, Future}

class DTDESystem(agents: Seq[DecentralizedAgent], environment: Environment){
  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit = {
      if (time > 0) {
        agents.foreach(_.step())
        environment.log()
        singleEpisode(time - 1)
      }
    }

    if (episodes > 0) {
      singleEpisode(episodeLength)
      environment.reset()
      agents.foreach(_.snapshot(episodes))
      learn(episodes - 1, episodeLength)
    } else {
      agents.foreach(_.logOnFile())
    }
  }

  final def runTest(episodeLength: Int, policy: PolicyNN): Unit = {
    agents.foreach(_.setTestPolicy(policy))
    episode(episodeLength)

    @tailrec
    def episode(time: Int): Unit = {
      agents.foreach(_.step())
      episode(time - 1)
    }
  }
}