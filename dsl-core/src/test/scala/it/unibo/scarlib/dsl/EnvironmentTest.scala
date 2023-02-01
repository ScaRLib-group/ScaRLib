package it.unibo.scarlib.dsl

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


@RunWith(classOf[JUnitRunner])
class EnvironmentTest extends AnyFlatSpec with Matchers {
  "Simple test" should "success" in {
      1 shouldBe 1
  }
}
