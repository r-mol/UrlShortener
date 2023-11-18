package urlshortener

import com.typesafe.scalalogging.LazyLogging
import org.mongodb.scala.{MongoCollection, Document}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.result.{InsertOneResult, DeleteResult}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MongoConnector(val urlCollection: MongoCollection[Document]) extends LazyLogging {
  def getDocumentByUrl(url: String): Option[Document] = {
    Await.result(urlCollection.find(equal("url", url)).first().toFutureOption(), Duration.Inf)
  }

  def getDocumentByShort(short: String): Option[Document] = {
    Await.result(urlCollection.find(equal("short", short)).first().toFutureOption(), Duration.Inf)
  }

  def insertDocument(document: Document): InsertOneResult = {
    Await.result(urlCollection.insertOne(document).toFuture(), Duration.Inf)
  }

  def deleteByUrl(url: String): DeleteResult = {
    Await.result(urlCollection.deleteOne(equal("url", url)).toFuture(), Duration.Inf)
  }

  def deleteByShort(short: String): DeleteResult = {
    Await.result(urlCollection.deleteOne(equal("short", short)).toFuture(), Duration.Inf)
  }

  def countDocuments(): Long = {
    Await.result(urlCollection.countDocuments().toFuture(), Duration.Inf)
  }
}
