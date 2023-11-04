import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15" % Test
  lazy val mongoScalaDriver = "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0"
  lazy val akkaHttp = "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.19.0-M10"

}
