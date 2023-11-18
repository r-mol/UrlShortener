package urlshortener

import com.typesafe.scalalogging.LazyLogging
import java.net.{URL, MalformedURLException}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.result.DeleteResult
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure, Random}

class UrlShortener(val mongoConnector: MongoConnector) extends LazyLogging {
  private val allowedChars = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toList

  def shortenUrl(url: String): Try[String] = {
    Try(new URL(url)) match {
      case Success(urlObj) =>
        Try {
          mongoConnector.getDocumentByUrl(urlObj.toString()).map(_.getString("short")).getOrElse {
            val short = getNextUniqueKey
            val mappingToInsert = Document("_id" -> new ObjectId(), "short" -> short, "url" -> url)
            mongoConnector.insertDocument(mappingToInsert)
            logger.info(s"Shortened URL: $urlObj -> $short")
            short
          }
        }
      case Failure(ex) =>
        ex match {
          case _: MalformedURLException =>
            logger.error(s"Invalid URL: $url")
            Failure(new MalformedURLException("Invalid URL"))
          case _ =>
            logger.error(s"Unknown error while shortening URL: $url", ex)
            Failure(ex)
        }
    }
  }

  def getUrl(short: String): Try[Option[URL]] = {
    Try {
      if (short.matches("^(?=.*[a-zA-Z0-9])[a-zA-Z0-9]*$")) {
        val url = mongoConnector.getDocumentByShort(short).map(document => new URL(document.getString("url")))
        logger.info(s"Get URL by short: $short -> result: ${url.getOrElse("Not found")}")
        url
      } else {
        logger.error(s"Invalid short string: $short")
        throw new IllegalArgumentException("Invalid short string")
      }
    }
  }

  private def deleteResultMessage(result: DeleteResult): String = {
    if (result.wasAcknowledged()) {
      s"Deleted ${result.getDeletedCount} document(s)."
    } else {
      "Delete request was not acknowledged."
    }
  }

  def deleteByUrl(url: String): DeleteResult = {
    val result = mongoConnector.deleteByUrl(url)
    logger.info(s"Deleted by URL: $url -> result: ${deleteResultMessage(result)}")
    result
  }

  def deleteByShort(short: String): DeleteResult = {
    val result = mongoConnector.deleteByShort(short)
    logger.info(s"Deleted by Short: $short -> result: ${deleteResultMessage(result)}")
    result
  }

  private def getNextUniqueKey: String = {
    @tailrec
    def loop(current: String, length: Int): String = {
      if (!mongoConnector.getDocumentByShort(current).contains(current)) current
      else if (isFullyUsed(length)) loop(generateRandomString(length + 1), length + 1)
      else loop(generateRandomString(length), length)
    }

    val count = mongoConnector.countDocuments()
    val initialLength =
      math.max(1, math.ceil(math.log(count.toDouble + 1) / math.log(allowedChars.size.toDouble)).toInt)

    loop(generateRandomString(initialLength), initialLength)
  }

  private def isFullyUsed(length: Int): Boolean = {
    val count = mongoConnector.countDocuments()

    math.pow(allowedChars.size.toDouble, length.toDouble) == count.toDouble
  }

  private def generateRandomString(length: Int): String = {
    val random = new Random()
    val sb = new StringBuilder(length)

    for (_ <- 1 to length) {
      sb.append(allowedChars(random.nextInt(allowedChars.size)))
    }

    sb.toString()
  }
}
