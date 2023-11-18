package urlshortener

import com.github.swagger.akka.model.Info

class SwaggerDocService(serverPort: Int) extends SwaggerHttpWithUiService {
  override val apiClasses: Set[Class[_]] = Set(classOf[Routes])
  override val host: String = s"localhost:$serverPort"
  override val info: Info = Info(version = "1.0")
}
