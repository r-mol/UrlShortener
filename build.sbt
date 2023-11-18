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

val akkaVersion = "2.6.21"
val akkaHttpVersion = "10.2.10"
val jacksonVersion = "2.15.2"
val swaggerVersion = "2.2.15"

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

      "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.0.0",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.11.0",
      "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.11.0",
      "com.github.swagger-akka-http" %% "swagger-enumeratum-module" % "2.8.0",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
      "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % swaggerVersion,

      "pl.iterators" %% "kebs-spray-json" % "1.9.5",
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.megard" %% "akka-http-cors" % "1.1.3",
      "org.slf4j" % "slf4j-simple" % "2.0.7",

      //"org.mockito" %% "mockito-scala" % "1.16.42" % Test
      "org.scalamock" %% "scalamock" % "5.1.0" % Test
    )
  )
