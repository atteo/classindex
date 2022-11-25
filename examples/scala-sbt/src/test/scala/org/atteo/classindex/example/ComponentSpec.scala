package org.atteo.classindex.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.atteo.classindex.ClassIndex

class ComponentSpec extends AnyFlatSpec with Matchers {
  "Component" should "be found" in {
    val components = ClassIndex.getAnnotated(classOf[Component])
    components.iterator().next() should be(classOf[FirstComponent])
  }
}
