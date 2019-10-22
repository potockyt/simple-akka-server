package works.samazama.validation

import akka.http.scaladsl.model.Uri
import cats.data.ValidatedNel
import cats.syntax.validated._

import scala.util.Try

case object InvalidUrl extends ValidationFailure("url", "URL is not valid")

object Rules {
  def validHttpUrl(url: String, vf: ValidationFailure): ValidatedNel[ValidationFailure, String] =
    Try {
      val uri = Uri.parseAbsolute(url)
      if (!List("http", "https").contains(uri.scheme)) throw new IllegalArgumentException
    }.fold(_ => vf.invalidNel, _ => url.validNel)
}
