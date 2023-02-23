package it.unibo.scarlib.core.model
/*
enum AgentMode:
    case Training
    case Testing
 */
sealed trait AgentMode

object AgentMode {
  case object Training extends AgentMode
  case object Testing extends AgentMode
}
