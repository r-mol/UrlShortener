package urlshortener

import com.typesafe.config.ConfigFactory
import org.mongodb.scala._

object Main extends App {
  val config = ConfigFactory.load()
  val serverAddress: String = config.getString("server.address")
  val serverPort: Int = config.getInt("server.port")
  val mongoUri: String = config.getString("mongo.uri")
  val mongoDbName: String = config.getString("mongo.database")
  val mongoCollection: String = config.getString("mongo.collection")

  val mongoClient: MongoClient = MongoClient(mongoUri)
  val mongoDatabase: MongoDatabase = mongoClient.getDatabase(mongoDbName)
  val urlCollection: MongoCollection[Document] = mongoDatabase.getCollection(mongoCollection)

  val urlShortener = new UrlShortener(new MongoConnector(urlCollection))

  new ApiService(serverAddress, serverPort, urlShortener)
}
