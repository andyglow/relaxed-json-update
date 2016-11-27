package com.github.andyglow.relaxed

import org.scalatest.MustMatchers._
import org.scalatest.WordSpec

abstract class AbstractRelaxedSpec(prefix: String) extends WordSpec {
  implicit def stringAsReader(json: String): Reader
  implicit val profileUpdater: Updater[Profile]

  s"$prefix.Relaxed.Update" must {
    "not affect entity if json is empty" in new Scope {
      (Relaxed(profile) updated """{}""") mustBe profile
    }
    "not affect entity if json is not empty, but there is no related fields" in new Scope {
      (Relaxed(profile) updated """{"foo": "bar"}""") mustBe profile
    }
    "affect entity's 'name' if json contains 'name' property" in new Scope {
      (Relaxed(profile) updated """{"name": "updated"}""") mustBe profile.copy(name = "updated")
    }
    "affect entity's 'alias' if json contains 'alias' property" in new Scope {
      (Relaxed(profile) updated """{"alias": "updated"}""") mustBe profile.copy(alias = Some("updated"))
    }
    "affect entity's 'alias' if json contains 'alias' property with value null" in new Scope {
      (Relaxed(profile) updated """{"alias": null}""") mustBe profile.copy(alias = None)
    }
    "affect entity's 'address/street' if json contains 'address/street' property" in new Scope {
      (Relaxed(profile) updated """{"address": { "street": "updated" }}""") mustBe profile.copy(address = profile.address.copy(street = "updated"))
    }
    "affect entity's 'phone/area' if json contains 'phone/area' property" in new Scope {
      (Relaxed(profile) updated """{"phone": { "area": "updated" }}""") mustBe profile.copy(
        phone = profile.phone.map(_.copy(area = "updated"))
      )
    }
    "affect entity's 'phone' if json contains 'phone' property" in new Scope {
      val noPhoneProfile = profile.copy(phone = None)
      (Relaxed(noPhoneProfile) updated """{"phone": { "area": "updated", "number": "updated" }}""") mustBe profile.copy(
        phone = Some(Phone("updated", "updated"))
      )
    }

    "affect entity's 'phone' if json contains 'phone' property with null" in new Scope {
      (Relaxed(profile) updated """{"phone": null}""") mustBe profile.copy(phone = None)
    }
  }

  trait Scope {
    val profile = Profile(
      id = "id",
      name = "name",
      address = Address(
        city = "city",
        street = "street",
        building = 1),
      alias = Some("alias"),
      phone = Some(Phone(
        area = "area",
        number = "number")))
  }

}
