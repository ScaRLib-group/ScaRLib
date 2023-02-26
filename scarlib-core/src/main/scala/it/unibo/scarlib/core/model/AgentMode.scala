package it.unibo.scarlib.core.model

sealed trait AgentMode

object AgentMode {
  case object Training extends AgentMode
  case object Testing extends AgentMode
}
