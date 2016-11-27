package com.github.andyglow.relaxed

import play.api.libs.json._

import scala.language.implicitConversions

class PlayJsonSpec extends AbstractRelaxedSpec("PlayJson") {
  import PlayJsonSupport._
  override implicit def stringAsReader(json: String): Reader = PlayJsonSupport(Json parse json)

  implicit val phoneReads = Json.reads[Phone]
  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]
}
