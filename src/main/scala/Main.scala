import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load()
  val serverAddress: String = config.getString("server.address")
  val serverPort: Int = config.getInt("server.port")
  val mongoUri: String = config.getString("mongo.uri")
  val mongoDbName: String = config.getString("mongo.database")
  val mongoCollection: String = config.getString("mongo.collection")

  val us = new UrlShortener(mongoUri, mongoDbName, mongoCollection)

  ApiService.startServer(serverAddress, serverPort, us)
}
