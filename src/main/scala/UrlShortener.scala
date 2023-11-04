import com.mongodb.client.result.DeleteResult
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.annotation.tailrec
import scala.util.Random
import java.net.URL

class UrlShortener(uri: String, dbName: String, collection: String) {
  val mongoClient: MongoClient = MongoClient(uri)
  val database: MongoDatabase = mongoClient.getDatabase(dbName)
  val urlCollection: MongoCollection[Document] = database.getCollection(collection)

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
