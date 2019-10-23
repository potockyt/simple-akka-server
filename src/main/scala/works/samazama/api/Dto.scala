package works.samazama.api

import works.samazama.validation.{InvalidUrl, Rules, Validatable}

object Dto {
  // out
  final case class EpochTime(epochMillis: Long)

  // in
  final case class Fetch(url: String) extends Validatable[Fetch] {
    override def validate: ValidationResult[Fetch] = (Rules.validHttpUrl(url, InvalidUrl)).map(Fetch)
  }
}
