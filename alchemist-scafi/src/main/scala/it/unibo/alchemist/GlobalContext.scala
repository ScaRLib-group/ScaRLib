package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.nodes.NodeManager
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position}

trait GlobalContext[T, P <: Position[P]] {
    def agents: List[Node[T]]

    def managers: List[NodeManager]

    def environment: Environment[T, P]
}
