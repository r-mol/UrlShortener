package urlshortener

import akka.http.scaladsl.server.{Directives, Route}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{DELETE, GET, Path, Produces}
import java.net.URL
import com.mongodb.client.result.DeleteResult

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.StatusCodes


@Path("/")
class MyRoutes (serverPort: Int, us: UrlShortener)extends Directives {
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
        complete {
          val short = us.shortenUrl(new URL(urlString))
          s"http://localhost:$serverPort/redirect/$short\n"
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
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Redirect by short URL",
    description = "Redirect to the original URL using the short URL",
    parameters = Array(
      new Parameter(name = "short", in = ParameterIn.PATH, description = "The short URL", required = true)
    ),
    responses = Array(
      new ApiResponse(responseCode = "302", description = "Redirect response"),
      new ApiResponse(responseCode = "404", description = "Not found")
    )
  )
  def redirectRoute: Route = {
    path("redirect" / Segment) { short =>
      get {
        us.getUrl(short) match {
          case Some(url) => redirect(Uri(url.toString()), StatusCodes.PermanentRedirect)
          case None      => complete(StatusCodes.NotFound, "Short URL not found.")
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