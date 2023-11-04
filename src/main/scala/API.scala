import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.Uri
import com.mongodb.client.result.DeleteResult
import java.net.URL

object ApiService {
  import system.dispatcher

  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  def deleteResultMessage(result: DeleteResult): String = {
    if (result.wasAcknowledged()) {
      s"Deleted ${result.getDeletedCount} document(s)."
    } else {
      "Delete request was not acknowledged."
    }
  }

  def startServer(serverAddress: String, serverPort: Int, us: UrlShortener): Unit = {
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

    val server = Http().newServerAt(serverAddress, serverPort).bind(route)

    println("Server online. Press RETURN to stop.")
    scala.io.StdIn.readLine()

    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
