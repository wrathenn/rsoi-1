ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val versions = new {
  val cats = "3.5.0"
  val http4s = "0.23.23"
  val circe = "0.14.5"
  val fs2 = "3.7.0"
  val doobie = "1.0.0-RC4"
  val betterMonadicFor = "0.3.1"
  val pureConfig = "0.17.4"
}

libraryDependencies += "org.typelevel" %% "cats-effect" % versions.cats

libraryDependencies ++= Seq(
  "http4s-dsl",
  "http4s-circe",
  "http4s-ember-client",
  "http4s-ember-server",
).map("org.http4s" %% _ % versions.http4s)

libraryDependencies ++= Seq(
  "circe-core",
  "circe-generic",
  "circe-parser",
).map("io.circe" %% _ % versions.circe)

libraryDependencies ++= Seq(
  "fs2-core",
  "fs2-io",
).map("co.fs2" %% _ % versions.fs2)

libraryDependencies ++= Seq(
  "doobie-core",
  "doobie-hikari",
  "doobie-postgres",
  "doobie-specs2",
).map("org.tpolecat" %% _ % versions.doobie)

libraryDependencies ++= Seq(
  "pureconfig-cats-effect",
  "pureconfig",
  "pureconfig-http4s",
  "pureconfig-yaml",
).map("com.github.pureconfig" %% _ % versions.pureConfig)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % versions.betterMonadicFor)



libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15",
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0",
  "org.typelevel" %% "cats-effect-testing-core" % "1.5.0",
  "org.typelevel" %% "cats-effect-testing-specs2" % "1.5.0",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0",
).map(_ % Test)


lazy val app = (project in file("app"))
  .settings(
    assembly / mainClass := Some("wrathenn.persons.Main"),
    assembly / assemblyJarName := "persons.jar",
  )
