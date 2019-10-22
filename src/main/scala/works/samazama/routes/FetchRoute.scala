package works.samazama.routes

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpEntity.{Chunk, LastChunk}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import cats.data.Validated.Valid
import io.circe.generic.auto._
import org.slf4j.LoggerFactory
import works.samazama.api.{Dto, EmptyResponse, V1}
import works.samazama.directives.ApiDirectives

import scala.util.{Failure, Success}

object FetchRoute extends ApiDirectives {
  private val log = LoggerFactory.getLogger(this.getClass)

  def route: Route = sinceApi(V1) {
    import works.samazama.api.V1._
    setupResource("fetch") {
      pathEnd {
        post {
          entity(as[Valid[Dto.Fetch]]) {
            case _ @Valid(fetch) =>
              extractActorSystem { system =>
                complete(
                  HttpResponse(
                    entity = HttpEntity.Chunked(
                      ContentTypes.`application/octet-stream`,
                      Source
                        .single(fetch)
                        .map(fe => RequestBuilding.Get(fe.url) -> fe.url)
                        .via(Http(system).superPool[String]())
                        .flatMapConcat {
                          case (Success(response), _)  => response.entity.dataBytes
                          case (Failure(exception), _) => throw exception
                        }
                        .map(bytes => Chunk(bytes))
                        .concat(Source.single(LastChunk))
                        .recover {
                          case ex: Exception =>
                            log.error("Fetch url stream failure", ex)
                            LastChunk
                        }
                    )
                  )
                )

              }
            case _ =>
              complete(EmptyResponse(StatusCodes.BadRequest))
          }
        }
      }
    }
  }
}
