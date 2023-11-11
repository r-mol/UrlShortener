package urlshortener

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.mongodb.client.result.DeleteResult
import java.net.URL
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import sttp.tapir.{Endpoint, endpoint, query, stringBody}
import sttp.tapir.server.akkahttp._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.StatusCodes

// import com.github.swagger.akka.SwaggerHttpService
// import com.github.swagger.akka.model.Info

// class SwaggerDocService(serverAddress: String, serverPort: Int) extends SwaggerHttpService {
//   override val apiClasses: Set[Class[_]] = Set(classOf[ApiService])
//   override val host: String = s"$serverAddress:$serverPort"
//   override val info: Info = Info(version = "1.0")

//   val route: Route = ???
// }


class ApiService(serverAddress: String, serverPort: Int, us: UrlShortener) {

  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

   def deleteResultMessage(result: DeleteResult): String = {
    if (result.wasAcknowledged()) {
      s"Deleted ${result.getDeletedCount} document(s)."
    } else {
      "Delete request was not acknowledged."
    }
  }

  // Tapir endpoints
  val getShortEndpoint: Endpoint[String, Unit, String, Any] =
    endpoint.get
      .in("getShort")
      .in(query[String]("url"))
      .out(stringBody)

  val deleteByUrlEndpoint: Endpoint[String, Unit, String, Any] =
    endpoint.delete
      .in("deleteByUrl")
      .in(query[String]("url"))
      .out(stringBody)

  val deleteByShortEndpoint: Endpoint[String, Unit, String, Any] =
    endpoint.delete
      .in("deleteByShort")
      .in(query[String]("short"))
      .out(stringBody)

  // Route conversion
  val getShortRoute: Route =
    getShortEndpoint.toRoute(urlString =>
      Future.successful(Right({
        val short = us.shortenUrl(new URL(urlString))
        s"http://$serverAddress:$serverPort/$short\n"
      }))
    )

  val deleteByUrlRoute: Route =
    deleteByUrlEndpoint.toRoute(urlString => Future.successful(Right(deleteResultMessage(us.deleteByUrl(urlString)))))

  val deleteByShortRoute: Route =
    deleteByShortEndpoint.toRoute(short => Future.successful(Right(deleteResultMessage(us.deleteByShort(short)))))

  //val swaggerDocService = new SwaggerDocService(serverAddress, serverPort)

  // Combined route including the redirection
  val route :Route = concat(
    getShortRoute,
      deleteByUrlRoute,
      deleteByShortRoute,
      //swaggerDocService.route,
      path(Segment) { short =>
        us.getUrl(short) match {
          case Some(url) => redirect(Uri(url.toString()), StatusCodes.PermanentRedirect)
          case None      => complete(StatusCodes.NotFound, "Short URL not found.")
        }
      }
  )

  val serverFuture = Http().newServerAt(serverAddress, serverPort).bind(route)
  val serverBinding = Await.result(serverFuture, Duration.Inf)

  println(
    s"Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}"
  )

  Await.result(serverBinding.whenTerminated, Duration.Inf)
}