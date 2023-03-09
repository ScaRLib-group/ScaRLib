# ScaRLib -- Scala Multi-Agent Deep Reinforcement Learning Framework.
ScaRLib is a Scala library for defining collaborative learning systems with many agents. 
In particular, this library offers:
- Centralized and decentralized learning modes
- Typed DSL used for defining multi-agent learning tasks
- Binding with state-of-the-art deep learning libraries (torch)
- Integration with Alchemist (a large-scale multi-agent simulator) and ScaFi (an aggregate programming language) to define typical scenarios in collective adaptive  system.

## How to use it: 
The tool is published on Maven. 
To integrate it into your own repository, you need to add (using Gradle):
```
implementation("io.github.davidedomini:scarlib-core:1.5.0")
implementation("io.github.davidedomini:dsl-core:1.5.0")
```
This enables the possibility of creating learning systems. 
A sample of the DSL usage is as follows:
```
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
```
system.train(episodes = 1000, episodeLength = 100)
```
Furthermore, it is possible to  verify a created policy:
```
val network = PolicyNN(path, inputSize = ..., hiddenSize = ...)
system.runTest(episodeLength = 100, network)
```
