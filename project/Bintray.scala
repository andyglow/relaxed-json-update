import bintray.BintrayKeys._
import sbt.Keys._
import sbt._
import BuildSettings._

object Bintray {

  lazy val settings = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    bintrayReleaseOnPublish in ThisBuild := false,
    licenses += ("LGPL-3.0", url("https://www.gnu.org/licenses/lgpl-3.0.html")),
    bintrayPackageLabels := Seq("scala", "tools", "rest", "json", "relaxed", "partial"),
    bintrayRepository := "scala-tools",
    homepage := Some(url(s"http://github.com/andyglow/$projectId")),
    checksums := Seq(),
    pomExtra :=
      <scm>
        <url>git://github.com/andyglow/${projectId}.git</url>
        <connection>scm:git://github.com/andyglow/${projectId}.git</connection>
      </scm>
        <developers>
          <developer>
            <id>andyglow</id>
            <name>Andrey Onistchuk</name>
            <url>https://ua.linkedin.com/in/andyglow</url>
          </developer>
        </developers>
  )

}