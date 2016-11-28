import sbt._
import Keys._

object BuildSettings {
  val ver = "0.0.1"
  val projectId = "relaxed-json-update"

  lazy val noSources = Seq(
    unmanagedSourceDirectories in Compile := Seq.empty[File],
    unmanagedSourceDirectories in Test := Seq.empty[File],
    unmanagedResourceDirectories in Compile := Seq.empty[File],
    unmanagedResourceDirectories in Test := Seq.empty[File])

  lazy val commonSettings = Seq(
    organization := "com.github.andyglow",
    version := ver,
    scalaVersion := "2.11.8",
    // crossScalaVersions  := Seq("2.11.8", "2.12.0"),

    libraryDependencies += (scalaVersion apply ("org.scala-lang" % "scala-reflect" % _ % Compile)).value,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1" % Test,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test,

    scalacOptions in Compile ++= Seq("-unchecked", "-deprecation", "-target:jvm-1.8", "-Ywarn-unused-import"),
    scalacOptions in(Compile, doc) ++= Seq("-unchecked", "-deprecation", "-implicits", "-skip-packages", "samples"),
    scalacOptions in(Compile, doc) ++= Opts.doc.title("Relaxed Json Update"),
    scalacOptions in(Compile, doc) ++= Opts.doc.version(ver)) ++ Bintray.settings

}