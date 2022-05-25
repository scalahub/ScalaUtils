ThisBuild / organization := "io.github.scalahub"
ThisBuild / organizationName := "scalahub"
ThisBuild / organizationHomepage := Some(url("https://github.com/scalahub"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/scalahub/ScalaUtils"),
    "scm:git@github.scalahub/ScalaUtils.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "scalahub",
    name = "scalahub",
    email = "scalahub@gmail.com",
    url = url("https://github.com/scalahub")
  )
)

ThisBuild / description := "Miscellaneous tools for Scala"
ThisBuild / licenses := List(
  "The Unlicense" -> new URL("https://unlicense.org/")
)
ThisBuild / homepage := Some(url("https://github.com/scalahub/ScalaUtils"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  Some("snapshots" at nexus + "content/repositories/snapshots")
}

ThisBuild / publishMavenStyle := true

ThisBuild / versionScheme := Some("early-semver")
