package com.github.andyglow.relaxed

import spray.json._

import scala.language.implicitConversions

class SprayJsonSpec extends AbstractRelaxedSpec("SprayJson") with DefaultJsonProtocol {
  import SprayJsonSupport._

  override implicit def stringAsReader(json: String): Reader = SprayJsonSupport(json.parseJson)

  implicit val phoneReads = jsonFormat2(Phone)
  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]
}
