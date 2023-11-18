package urlshortener

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

class ApiService(serverAddress: String, serverPort: Int, urlShortener: UrlShortener) {
  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val routes = cors()(new Routes(serverPort, urlShortener).route ~ new SwaggerDocService(serverPort).routes)

  val serverFuture = Http().newServerAt(serverAddress, serverPort).bind(routes)
  val serverBinding = Await.result(serverFuture, Duration.Inf)

  println(
    s"Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}"
  )

  Await.result(serverBinding.whenTerminated, Duration.Inf)
}
