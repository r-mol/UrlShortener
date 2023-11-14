package urlshortener

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.concurrent.duration.Duration
import scala.concurrent.Await


class ApiService(serverAddress: String, serverPort: Int, us: UrlShortener) {

  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  val swaggerDocService = new SwaggerDocService()
  val my = new MyRoutes(serverAddress, serverPort, us)


  val serverFuture = Http().newServerAt(serverAddress, serverPort).bind(swaggerDocService.routes ~ my.route)
  val serverBinding = Await.result(serverFuture, Duration.Inf)

  println(
    s"Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}"
  )

  Await.result(serverBinding.whenTerminated, Duration.Inf)
}