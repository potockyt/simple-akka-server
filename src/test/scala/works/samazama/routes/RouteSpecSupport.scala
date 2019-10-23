package works.samazama.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, ModeledHeader}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.collection.immutable.Seq

trait RouteSpecSupport extends FailFastCirceSupport {
  def mediaType(version: Int = 1): MediaType.WithFixedCharset =
    MediaType.applicationWithFixedCharset(s"vnd.samazamaworks.v$version+json", HttpCharsets.`UTF-8`)

  override def mediaTypes: Seq[MediaType.WithFixedCharset] = List(mediaType(), MediaTypes.`application/json`)

  val contentTypeVN: ContentType.WithFixedCharset = mediaType().toContentType
  val acceptVN: ModeledHeader = Accept(mediaType())
  val defaultApiHeaders: List[HttpHeader] = List(acceptVN)
}
