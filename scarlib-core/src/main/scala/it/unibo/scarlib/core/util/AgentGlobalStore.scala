/*
 * ScaRLib: A Framework for Cooperative Many Agent Deep Reinforcement learning in Scala
 * Copyright (C) 2023, Davide Domini, Filippo Cavallari and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of ScaRLib, and is distributed under the terms of the
 * MIT License as described in the file LICENSE in the ScaRLib distribution's top directory.
 */

package it.unibo.scarlib.core.util

import scala.reflect.ClassTag

/** A container of agents data to be logged */
trait AgentGlobalStore {
  def put(who: Int, tag: String, data: Any): Unit
  def filterByType[T: ClassTag]: Map[Int, Map[String, T]]
  def clearAll(): Unit
}

object AgentGlobalStore {
  def apply() = instance

  private val instance: AgentGlobalStore = new AgentGlobalStore {
    private var map = Map.empty[Int, Map[String, Any]]
    override def put(who: Int, tag: String, data: Any): Unit =
      map += who -> (map.getOrElse(who, Map.empty) + (tag -> data))

    override def filterByType[T: ClassTag]: Map[Int, Map[String, T]] = {
      val classTag = implicitly[ClassTag[T]]
      map.map { case (who, data) =>
        who -> data
          .filter { case (_, value) => classTag.runtimeClass.isInstance(value) }
          .map { case (tag, value) => tag -> value.asInstanceOf[T] }
      }
    }

    override def clearAll(): Unit = this.map = Map.empty
  }

  def averageAllNumeric(store: AgentGlobalStore): Map[String, Double] = {
    val filtered = store.filterByType[Number]
    filtered
      .map { case (who, data) =>
        who -> data.map { case (tag, value) => tag -> value.doubleValue() }
      }
      .flatMap { case (id, data) => data.map { case (key, value) => (id, key) -> value } }
      .groupMap(_._1._2)(_._2)
      .map { case (key, values) => key -> values.sum / values.size }
  }

  def sumAllNumeric(store: AgentGlobalStore): Map[String, Double] = {
    val filtered = store.filterByType[Number]
    val flatten = filtered
      .map { case (who, data) =>
        who -> data.map { case (tag, value) => tag -> value.doubleValue() }
      }
      .flatMap { case (id, data) => data.map { case (key, value) => (id, key) -> value } }
    flatten.groupMapReduce(_._1._2)(_._2)(_ + _)
  }
}
