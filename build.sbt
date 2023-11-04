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
      akkaHttp,
      "com.typesafe" % "config" % "1.4.1",
      "com.typesafe.akka" %% "akka-http" % "10.2.4",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.17.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.17.0",
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.17.0",
      "io.circe" %% "circe-generic" % "0.13.0",
      "io.circe" %% "circe-parser" % "0.13.0"
    )
  )
