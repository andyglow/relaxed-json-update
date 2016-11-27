package com.github.andyglow.relaxed

import upickle._

import scala.language.implicitConversions

class UpickleSpec extends AbstractRelaxedSpec("Upickle") {
  import UpickleSupport._
  override implicit def stringAsReader(x: String): Reader = UpickleSupport(json read x)

  implicit val phoneReads = upickle.default.macroR[Phone]
  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]
}
