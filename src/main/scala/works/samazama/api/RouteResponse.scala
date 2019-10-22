package works.samazama.api

import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpProtocols, HttpResponse, StatusCode, StatusCodes}

import scala.collection.immutable.Seq

trait RouteResponse

object EmptyResponse {

  def apply(status: StatusCode = StatusCodes.OK, headers: Seq[HttpHeader] = Nil): HttpResponse =
    new HttpResponse(
      status,
      headers,
      // Prevents rendering of Content-Type header
      HttpEntity.Empty,
      HttpProtocols.`HTTP/1.1`
    )
}
