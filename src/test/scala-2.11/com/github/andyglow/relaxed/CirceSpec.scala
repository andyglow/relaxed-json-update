package com.github.andyglow.relaxed

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

import scala.language.implicitConversions

class CirceSpec extends AbstractRelaxedSpec("Circe") {
  import CirceSupport._
  override implicit def stringAsReader(x: String): Reader = CirceSupport(parser.parse(x).right.get)

  implicit val phoneUpdater: Updater[Phone] = Relaxed.updater[Phone]
  implicit val addressUpdater: Updater[Address] = Relaxed.updater[Address]
  override implicit val profileUpdater: Updater[Profile] = Relaxed.updater[Profile]
}
