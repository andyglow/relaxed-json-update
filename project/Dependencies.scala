import sbt._

object Dependencies {
  val playVersion     = "2.5.10"
  val sprayVersion    = "1.3.2"
  val json4sVersion   = "3.5.0"
  val argonautVersion = "6.1"
  val upickleVersion  = "0.4.3"
  val jacksonScalaModuleVersion = "2.8.4"
  val circeVersion    = "0.6.1"

  val playJson  = "com.typesafe.play" %% "play-json"      % playVersion     % Compile
  val sprayJson = "io.spray"          %% "spray-json"     % sprayVersion    % Compile
  val json4s    = "org.json4s"        %% "json4s-native"  % json4sVersion   % Compile
  val argonaut  = "io.argonaut"       %% "argonaut"       % argonautVersion % Compile
  val upickle   = "com.lihaoyi"       %% "upickle"        % upickleVersion  % Compile
  val jackson   = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonScalaModuleVersion % Compile
  val circe     = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser").map(_ % circeVersion % Compile)
}