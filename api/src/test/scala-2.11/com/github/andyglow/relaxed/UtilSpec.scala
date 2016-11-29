package com.github.andyglow.relaxed

import org.scalatest.WordSpec
import org.scalatest.MustMatchers._

class UtilSpec extends WordSpec {
  import UtilSpec._

  "Util" must {
    "enumerate fields" in {
      val fields = Util.fieldMap[UtilSpec.Ex1]
      fields mustBe resultingMap
    }
    "not enumerate fields to skip" in {
      val fields = Util.fieldMap[UtilSpec.Ex2]
      fields mustBe resultingMap
    }
  }

}

object UtilSpec {
  case class Ex1(foo: String, bar: Int)
  case class Ex2(@skip baz: Long, foo: String, bar: Int)

  val resultingMap = Map("foo" -> classOf[String], "bar" -> classOf[Int])
}
