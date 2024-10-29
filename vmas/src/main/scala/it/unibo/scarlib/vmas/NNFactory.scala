package it.unibo.scarlib.vmas

import it.unibo.scarlib.core.neuralnetwork.{DQNAbstractFactory, SimpleSequentialDQN}
import me.shadaj.scalapy.py

class NNFactory(stateDescriptor: VmasStateDescriptor, actionsSpace: Seq[VMASAction]) extends DQNAbstractFactory[py.Dynamic] {

        override def createNN(): py.Dynamic = {
            SimpleSequentialDQN(stateDescriptor.getSize, 64, actionsSpace.size)
        }

}

