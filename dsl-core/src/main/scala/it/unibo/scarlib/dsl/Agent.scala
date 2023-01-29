package it.unibo.scarlib.dsl

trait Agent 

class SimpleAgent extends Agent

case class AgentReference[A <: Agent](ref: Class[A]):
  