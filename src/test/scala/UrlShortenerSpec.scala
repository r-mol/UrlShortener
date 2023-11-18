package urlshortener

import com.mongodb.client.result.{InsertOneResult, DeleteResult}
import org.mongodb.scala.{MongoCollection, Document}
import org.mongodb.scala.bson.BsonInt32
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import java.net.{MalformedURLException, URL}

class UrlShortenerSpec extends AnyFlatSpec with Matchers with MockFactory {
  val connector = mock[MongoConnector]
  val shortener = new UrlShortener(connector)

  "Shorten URL" should "succeed for a existing URL in database" in {
    val url = "https://example.com"

    (connector
      .getDocumentByUrl(_: String))
      .expects(url)
      .returns(Option(Document("_id" -> 1, "short" -> "someshort", "url" -> url.toString())))

    val result = shortener.shortenUrl(url)

    assert(result.isSuccess)
    assert(result.get.equals("someshort"))
  }

  it should "succeed for a new URL and return a short ID" in {
    val url = "https://example.com"

    (connector
      .getDocumentByUrl(_: String))
      .expects(url)
      .returns(None)

    (connector
      .getDocumentByShort(_: String))
      .expects(*)
      .returns(None)

    (connector.countDocuments _)
      .expects()
      .returns(0)

    (connector
      .insertDocument(_: Document))
      .expects(*)
      .returns(InsertOneResult.acknowledged(BsonInt32(1)))

    val result = shortener.shortenUrl(url)

    assert(result.isSuccess)
  }

  it should "fail for a invalid URL" in {
    val url = "invalid_url"

    val result = shortener.shortenUrl(url)

    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[MalformedURLException])
  }

  "Get Url" should "return the URL for a valid short string" in {
    val short = "validShort"
    val url = new URL("https://example.com")

    (connector.getDocumentByShort _)
      .expects(short)
      .returns(Option(Document("url" -> url.toString())))

    val result = shortener.getUrl(short)

    assert(result.isSuccess)
    assert(result.get.get.equals(url))
  }

  it should "throw an IllegalArgumentException for an invalid short string" in {
    val short = "invalid_Short"

    val result = shortener.getUrl(short)

    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[IllegalArgumentException])
  }

  it should "throw an IllegalArgumentException for an empty short string" in {
    val short = ""

    val result = shortener.getUrl(short)

    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[IllegalArgumentException])
  }

  it should "throw an IllegalArgumentException for an space as a short string" in {
    val short = " "

    val result = shortener.getUrl(short)

    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[IllegalArgumentException])
  }

  it should "return empty succes" in {
    val short = "validShort"

    (connector.getDocumentByShort _)
      .expects(short)
      .returns(None)

    val result = shortener.getUrl(short)

    assert(result.isSuccess)
    assert(result.get.isEmpty)
  }
}
