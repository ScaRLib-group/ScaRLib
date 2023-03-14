# ScaRLib -- Scala Multi-Agent Deep Reinforcement Learning Framework.
ScaRLib is a Scala library for defining collaborative learning systems with many agents, namely: CMARL systems. 
In particular, this library offers:
- Centralized and decentralized learning modes
- Typed DSL used for defining multi-agent learning tasks
- Binding with state-of-the-art deep learning libraries (torch)
- Integration with Alchemist (a large-scale multi-agent simulator) and ScaFi (an aggregate programming language) to define typical scenarios in collective adaptive  system.

## ScaRLib submodules

![scarlib-modules](.img/scarlib-modules.png)

### ScaRLib Core
The module `scarlib-core` implements all the abstractions that model the CMARL domain. 
The key element is the `system`, it might be of two different types: 
    i) Centralized Training Decentralized Execution system (`CTDESystem`)
    ii) Decentralized Training Decentralized Execution system (`DTDESystem`).
Basically, the system, is a collection of agents that interact within a shared environment
and that are trained to optimize a global or local reward signal expressed by
a reward function. 
Through this definition, we have mentioned the remaining concepts of the CMARL domain,
therefore, to create an experiment, it is necessary to define six basic elements:

- Action space: the set of actions each agent can perform, it could be easily defined 
        extending the trait `Action`, for example:
  ```scala
  object ActionSpace {
    case object North extends Action
    case object South extends Action
    case object East extends Action
    case object West extends Action
  
    def toSeq() = Seq(North, South, East, West)
  }
  ```
  
- State: represents all the information an agent knows about the Environment at a certain time, 
    it must extend the trait `State`

- Reward function: defines how good is an action given the state in which the agent is
     ```scala
  class SimpleRewardFunction() extends RewardFunction {
        def compute(currentState: State, action: Action, newState: State): Double = ???
  }
  ```
  
- Environment: defines how ???

- Dataset: the storage for the experience accumulated over the time by the agents. 
    The tool provides a simple buffered queue, if needed a user might implement his own dataset 
    extending the trait `ReplayBuffer`

- Agents: the number of agents involved in the experiment

Todo: 
learning params +
torch

### Alchemist - Scafi
This module provides the bindings with two state-of-the-art tools, 
    namely: [Scafi](https://github.com/scafi/scafi) 
    and [Alchemist](http://alchemistsimulator.github.io/).
The integration of these two tools is a game-changer because it introduces 
    significant potential in ScaRLib: 
    i) Scafi enables the usage of the Aggregate Programming paradigms 
        to express collective behaviours for the agents
    ii) Alchemist enables the definition of large-scale sets of agents 
        in complex distributed systems (e.g., swarm robotics).

The definition of an experiment does not change significantly, only two elements are added:

- Alchemist simulation definition: basically it is a YAML file containing the description 
    of the alchemist environment, for example:
    ```yaml
    incarnation: scafi
    network-model:
        type: ConnectWithinDistance
        parameters: [0.5]
    deployments:
        type: Grid
        parameters: [-5,-5,5,5,0.25,0.25]
    programs:
        - program:
        - time-distribution: 1
          type: Event
          actions:
          - type: RunScafiProgram
            parameters: [program]
        - program: send
    ```
- Aggregate program: the Scafi program that express the aggregate logic. For example,
    if we want express the state as the distances from the neighbours:
    ```scala
      val state = foldhoodPlus(Seq.empty)(_ ++ _)(Set(nbrVector))
    ```
### DSL Core

## How to use it:
The tool is published on Maven. 
To integrate it into your own repository, you need to add (using Gradle):
```kotlin
implementation("io.github.davidedomini:scarlib-core:1.5.0")
implementation("io.github.davidedomini:dsl-core:1.5.0")
```
This enables the possibility of creating learning systems. 
A sample of the DSL usage is as follows:
```scala
val system = learningSystem {
    rewardFunction { new MyRewardFunction() } 
    actions { MyAction.all} // action supported by the agent
    dataset { ReplayBuffer[State, Action](10000) } // shared memory
    agents { 50 } // select the number of agent
    environment {
        // select a specific environment
        "it.unibo.scarlib.experiments.myEnvironment"
    }
}
```
With this system, it is possible to start learning process:
```scala
system.train(episodes = 1000, episodeLength = 100)
```
Furthermore, it is possible to  verify a created policy:
```scala
val network = PolicyNN(path, inputSize = ..., hiddenSize = ...)
system.runTest(episodeLength = 100, network)
```
