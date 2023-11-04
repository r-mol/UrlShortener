import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.Uri
import com.mongodb.client.result.DeleteResult
import java.net.URL
import scala.concurrent.duration.Duration
import scala.concurrent.Await

class ApiService(serverAddress: String, serverPort: Int, us: UrlShortener) {

  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  def deleteResultMessage(result: DeleteResult): String = {
    if (result.wasAcknowledged()) {
      s"Deleted ${result.getDeletedCount} document(s)."
    } else {
      "Delete request was not acknowledged."
    }
  }

  val route =
    path("getShort") {
      parameter("url") { urlString =>
        val short = us.shortenUrl(new URL(urlString))
        complete(s"http://$serverAddress:$serverPort/$short")
      }
    } ~
      path("deleteByUrl") {
        parameter("url") { urlString =>
          val deleteResult = us.deleteByUrl(urlString)
          complete(deleteResultMessage(deleteResult))
        }
      } ~
      path("deleteByShort") {
        parameter("short") { short =>
          val deleteResult = us.deleteByShort(short)
          complete(deleteResultMessage(deleteResult))
        }
      } ~
      path(Segment) { short =>
        us.getUrl(short) match {
          case Some(url) => redirect(Uri(url.toString()), StatusCodes.PermanentRedirect)
          case None      => complete(StatusCodes.NotFound, "Short URL not found.")
        }
      }

  val serverFuture = Http().newServerAt(serverAddress, serverPort).bind(route)
  val serverBinding = Await.result(serverFuture, Duration.Inf)

  println(
    s"Server started at http://${serverBinding.localAddress.getHostString}:${serverBinding.localAddress.getPort}"
  )

  Await.result(serverBinding.whenTerminated, Duration.Inf)
}