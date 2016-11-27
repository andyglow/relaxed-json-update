package com.github.andyglow.relaxed

import argonaut._, Argonaut._

import scala.language.implicitConversions

class ArgonautSpec extends AbstractRelaxedSpec("Argonaut") {
  import ArgonautSupport._

  override implicit def stringAsReader(json: String): Reader = ArgonautSupport(json.parseOption.get)

  implicit val phoneReads: CodecJson[Phone] = casecodec2(Phone.apply, Phone.unapply)("area", "number")
  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]
}
