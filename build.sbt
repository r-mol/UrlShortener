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

lazy val swaggerDependencies = Seq(
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.0.0",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.11.0",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.11.0",
  "com.github.swagger-akka-http" %% "swagger-enumeratum-module" % "2.8.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
  "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % "2.2.15"
)

lazy val akkaDependencies = Seq(
  "pl.iterators" %% "kebs-spray-json" % "1.9.5",
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "com.typesafe.akka" %% "akka-actor" % "2.6.21",
  "com.typesafe.akka" %% "akka-stream" % "2.6.21",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.21",
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  "org.slf4j" % "slf4j-simple" % "2.0.7",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.17.0-M9",
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.17.0-M9"
)

lazy val root = (project in file("."))
  .settings(
    name := "tree",
    libraryDependencies ++= Seq(
      scalaTest,
      mongoScalaDriver,
      scalaMock,
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    ) ++ akkaDependencies ++ swaggerDependencies
  )
