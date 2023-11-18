package urlshortener

import akka.http.scaladsl.model.StatusCodes.PermanentRedirect
import com.github.swagger.akka.SwaggerHttpService

trait SwaggerHttpWithUiService extends SwaggerHttpService {
  val swaggerUiRoute = {
    pathPrefix("docs") {
      val pathInit = removeTrailingSlashIfNecessary(apiDocsPath)
      redirect(s"http://localhost:80/?url=http://localhost:12345/$pathInit/swagger.json", PermanentRedirect)
    }
  }

  override val routes = super.routes ~ swaggerUiRoute

  private def removeTrailingSlashIfNecessary(path: String): String =
    if(path.endsWith("/")) removeTrailingSlashIfNecessary(path.substring(0, path.length - 1)) else path
}
