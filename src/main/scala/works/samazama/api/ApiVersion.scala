package works.samazama.api

import akka.http.scaladsl.model.{HttpCharsets, MediaType, MediaTypes}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.collection.immutable.Seq

trait ApiVersion extends FailFastCirceSupport {
  def version: String

  def `application/samazamaworks+json`: MediaType.WithFixedCharset =
    MediaType.applicationWithFixedCharset(s"vnd.samazamaworks.$version+json", HttpCharsets.`UTF-8`)
}

trait ApiV1 extends ApiVersion {
  override def version: String = "v1"

  override def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/samazamaworks+json`, MediaTypes.`application/json`)
}
