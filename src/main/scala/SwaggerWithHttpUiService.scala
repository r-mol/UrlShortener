package urlshortener

import akka.http.scaladsl.model.StatusCodes.PermanentRedirect
import com.github.swagger.akka.SwaggerHttpService

/**
 * SwaggerHttpService along with swagger-ui.
 *
 * Swagger-UI is now delegated to https://petstore.swagger.io but with a 'url' parameter that points
 * at the 'swagger.json' generated by this example.
 */
trait SwaggerHttpWithUiService extends SwaggerHttpService {

  val swaggerUiRoute = {
    pathPrefix(apiDocsPath) {
      val pathInit = removeTrailingSlashIfNecessary(apiDocsPath)
      redirect(s"https://petstore.swagger.io/?url=http://localhost:1234/$pathInit/swagger.json", PermanentRedirect)
    }
  }

  override val routes = super.routes ~ swaggerUiRoute

  private def removeTrailingSlashIfNecessary(path: String): String =
    if(path.endsWith("/")) removeTrailingSlashIfNecessary(path.substring(0, path.length - 1)) else path

}