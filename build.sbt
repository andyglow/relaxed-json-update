import BuildSettings._

lazy val api = (project in file("api"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-api")

lazy val `play-json` = (project in file("play-json"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-play-json")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.playJson))

lazy val `spray-json` = (project in file("spray-json"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-spray-json")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.sprayJson))

lazy val json4s = (project in file("json4s"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-json4s")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.json4s))

lazy val argonaut = (project in file("argonaut"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-argonaut")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.argonaut))

lazy val upickle = (project in file("upickle"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-upickle")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.upickle))

lazy val jackson = (project in file("jackson"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-jackson")
  .dependsOn(api)
  .settings(libraryDependencies ++= Seq(
    Dependencies.jackson))

lazy val circe = (project in file("circe"))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update-circe")
  .dependsOn(api)
  .settings(libraryDependencies ++= Dependencies.circe)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(name := "relaxed-json-update")
  .dependsOn(api, `play-json`, `spray-json`, json4s, argonaut, upickle, jackson, circe)
  .aggregate(api, `play-json`, `spray-json`, json4s, argonaut, upickle, jackson, circe)