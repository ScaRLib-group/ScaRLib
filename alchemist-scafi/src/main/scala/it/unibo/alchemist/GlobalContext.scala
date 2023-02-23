package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position}
import it.unibo.scarlib.core.model.Result

import scala.jdk.CollectionConverters.CollectionHasAsScala

trait GlobalContext[T, P <: Position[P]] {
    def agents: List[Node[T]]

    def managers: List[NodeManager]

    def environment: Environment[T, P]

    def stateAndAction: (Seq[Result]) = {
        agents
          .map(_.getContents.values().asScala)
          .map(_.filter(_.isInstanceOf[Result]).head)
          .map(_.asInstanceOf[Result])

    }
}
