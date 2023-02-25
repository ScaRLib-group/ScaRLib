package it.unibo.scarlib.core.deepRL

import scala.annotation.tailrec
import it.unibo.scarlib.core.model.GeneralEnvironment

class DTDESystem(agents: Seq[DecentralizedAgent], environment: GeneralEnvironment){
  @tailrec
  final def learn(episodes: Int, episodeLength: Int): Unit = {
    @tailrec
    def singleEpisode(time: Int): Unit = {
      if (time > 0) {
        agents.foreach(_.step())
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
}

