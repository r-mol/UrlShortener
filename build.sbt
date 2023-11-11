import Dependencies._

ThisBuild / scalaVersion     := "2.13.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"

Compile / compile / scalacOptions ++= Seq(
  "-Werror",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused",
  "-Wvalue-discard",
  "-Xlint",
  "-Xlint:-byname-implicit",
  "-Xlint:-implicit-recursion",
  "-unchecked",
)

lazy val root = (project in file("."))
  .settings(
    name := "tree",
    libraryDependencies ++= Seq(
      scalaTest,
      mongoScalaDriver,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.17.0-M9",
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.17.0-M9",
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.2.0"
    )
  )
