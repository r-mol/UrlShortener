import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.mongodb.client.result.DeleteResult
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.net.URL
import scala.annotation.tailrec
import scala.util.Random
import akka.http.scaladsl.model.Uri

object UrlShortener {
  val mongoClient: MongoClient = MongoClient("mongodb+srv://TestUser:TestPassword@cluster85675.wp3r08d.mongodb.net")
  val database: MongoDatabase = mongoClient.getDatabase("urlDB")
  val urlCollection: MongoCollection[Document] = database.getCollection("url_collection")

  private val allowedChars = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toList

  def shortenUrl(url: URL): String = {
    getDocumentByUrl(url.toString()).map(_.getString("short")).getOrElse {
      val short = getNextUniqueKey
      val mappingToInsert = Document("_id" -> new ObjectId(), "short" -> short, "url" -> url.toString())
      Await.result(urlCollection.insertOne(mappingToInsert).toFuture(), Duration.Inf)
      short
    }
  }

  def getUrl(short: String): Option[URL] = {
    getDocumentByShort(short).map(document => new URL(document.getString("url")))
  }

  private def getDocumentByUrl(url: String): Option[Document] = {
    Await.result(urlCollection.find(equal("url", url)).first().toFutureOption(), Duration.Inf)
  }

  private def getDocumentByShort(short: String): Option[Document] = {
    Await.result(urlCollection.find(equal("short", short)).first().toFutureOption(), Duration.Inf)
  }

  def deleteByUrl(url: String): DeleteResult = {
    Await.result(urlCollection.deleteOne(equal("url", url)).toFuture(), Duration.Inf)
  }

  def deleteByShort(short: String): DeleteResult = {
    Await.result(urlCollection.deleteOne(equal("short", short)).toFuture(), Duration.Inf)
  }

  private def getNextUniqueKey: String = {
    @tailrec
    def loop(current: String, length: Int): String = {
      if (!getDocumentByShort(current).contains(current)) current
      else if (isFullyUsed(length)) loop(generateRandomString(length + 1), length + 1)
      else loop(generateRandomString(length), length)
    }

    val numberOfDocuments = urlCollection.countDocuments().toFuture()
    val count = Await.result(numberOfDocuments, Duration.Inf)

    val initialLength =
      math.max(1, math.ceil(math.log(count.toDouble + 1) / math.log(allowedChars.size.toDouble)).toInt)
    loop(generateRandomString(initialLength), initialLength)
  }

  private def isFullyUsed(length: Int): Boolean = {
    val numberOfDocuments = urlCollection.countDocuments().toFuture()
    val count = Await.result(numberOfDocuments, Duration.Inf)

    math.pow(allowedChars.size.toDouble, length.toDouble) == count.toDouble
  }

  private def generateRandomString(length: Int): String = {
    @tailrec
    def loop(current: List[Char], remaining: Int): List[Char] = {
      if (remaining == 0) current
      else loop(allowedChars(Random.nextInt(allowedChars.size)) :: current, remaining - 1)
    }

    loop(Nil, length).mkString
  }
}

object ApiService {

  import UrlShortener._

  implicit val system: ActorSystem = ActorSystem("url-shortener-system")

  import system.dispatcher

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
        val short = shortenUrl(new URL(urlString))
        complete(s"http://127.0.0.1/$short")
      }
    } ~
      path("deleteByUrl") {
        parameter("url") { urlString =>
          val deleteResult = deleteByUrl(urlString)
          complete(deleteResultMessage(deleteResult))
        }
      } ~
      path("deleteByShort") {
        parameter("short") { short =>
          val deleteResult = deleteByShort(short)
          complete(deleteResultMessage(deleteResult))
        }
      } ~
      path(Segment) { short =>
        getUrl(short) match {
          case Some(url) => redirect(Uri(url.toString()), StatusCodes.PermanentRedirect)
          case None      => complete(StatusCodes.NotFound, "Short URL not found.")
        }
      }

  def startServer(): Unit = {
    val server = Http().newServerAt("localhost", 8080).bind(route)

    println("Server online. Press RETURN to stop.")
    scala.io.StdIn.readLine()

    server
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

object Main extends App {
  ApiService.startServer()
}
