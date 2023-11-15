package urlshortener

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import akka.http.scaladsl.server.Directives._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}


class ApiService(serverAddress: String, serverPort: Int, us: UrlShortener) {
  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val routes =
    cors() (new MyRoutes(serverPort, us).route ~
     new SwaggerDocService().routes)

  val serverFuture = Http().newServerAt(serverAddress, serverPort).bind(routes)
  val serverBinding = Await.result(serverFuture, Duration.Inf)

  println(
    s"Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}"
  )

  Await.result(serverBinding.whenTerminated, Duration.Inf)
}