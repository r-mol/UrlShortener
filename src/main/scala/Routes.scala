package urlshortener

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.model.{Uri, StatusCodes}
import com.mongodb.client.result.DeleteResult
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{DELETE, GET, Path, Produces}
import java.net.MalformedURLException
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Path("/")
class Routes(serverPort: Int, us: UrlShortener) extends Directives {
  def deleteResultMessage(result: DeleteResult): String = {
    if (result.wasAcknowledged()) {
      s"Deleted ${result.getDeletedCount} document(s)."
    } else {
      "Delete request was not acknowledged."
    }
  }

  @GET
  @Path("getShort")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Get short URL",
    description = "Get the short URL for a given URL",
    parameters = Array(
      new Parameter(name = "url", in = ParameterIn.QUERY, description = "The original URL", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Successful response"),
      new ApiResponse(responseCode = "400", description = "Bad request")
    )
  )
  def getShortRoute: Route = {
    get {
      parameter("url") { urlString =>
        val shortFuture = Future.fromTry(us.shortenUrl(urlString))
        onComplete(shortFuture) {
          case Success(short) =>
            complete(s"http://localhost:$serverPort/redirect/$short\n")
          case Failure(ex) =>
            ex match {
              case _: MalformedURLException =>
                complete(StatusCodes.BadRequest, "Invalid URL")
              case _ =>
                complete(StatusCodes.InternalServerError, "Error while shortening URL")
            }
        }
      }
    }
  }

  @DELETE
  @Path("deleteByUrl")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Delete by URL",
    description = "Delete a URL by its original URL",
    parameters = Array(
      new Parameter(name = "url", in = ParameterIn.QUERY, description = "The original URL", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Successful response"),
      new ApiResponse(responseCode = "400", description = "Bad request")
    )
  )
  def deleteByUrlRoute: Route = {
    delete {
      parameter("url") { urlString =>
        complete {
          deleteResultMessage(us.deleteByUrl(urlString))
        }
      }
    }
  }

  @DELETE
  @Path("deleteByShort")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Delete by short URL",
    description = "Delete a URL by its short URL",
    parameters = Array(
      new Parameter(name = "short", in = ParameterIn.QUERY, description = "The short URL", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Successful response"),
      new ApiResponse(responseCode = "400", description = "Bad request")
    )
  )
  def deleteByShortRoute: Route = {
    delete {
      parameter("short") { short =>
        complete {
          deleteResultMessage(us.deleteByShort(short))
        }
      }
    }
  }

  @GET
  @Path("redirect/{short}")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Operation(
    summary = "Redirect by short URL",
    description = "Redirect to the original URL using the short URL",
    parameters = Array(
      new Parameter(name = "short", in = ParameterIn.PATH, description = "The short URL", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "302", description = "Redirect response"),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "404", description = "Not found")
    )
  )
  def redirectRoute: Route = {
    path("redirect" / Segment) { short =>
      get {
        val urlFuture = Future.fromTry(us.getUrl(short))
        onComplete(urlFuture) {
          case Success(Some(url)) =>
            redirect(Uri(url.toString), StatusCodes.PermanentRedirect)
          case Success(None) =>
            complete(StatusCodes.NotFound, "Short URL not found")
          case Failure(ex) =>
            ex match {
              case _: IllegalArgumentException =>
                complete(StatusCodes.BadRequest, "Invalid short string")
              case _ =>
                complete(StatusCodes.InternalServerError, "Error while getting original URL")
            }
        }
      }
    }
  }

  val route: Route = concat(
    getShortRoute,
    deleteByUrlRoute,
    deleteByShortRoute,
    redirectRoute
  )
}
