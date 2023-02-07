package it.unibo.scarlib.core.model

case class AgentReference[A <: Agent](ref: Class[A])