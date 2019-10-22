package works.samazama.api

import works.samazama.validation.{InvalidUrl, Rules, Validatable}

object Dto {
  // out
  final case class EpochMillis(epochMillis: Long = System.currentTimeMillis)

  // in
  final case class Fetch(url: String) extends Validatable[Fetch] {
    override def validate: ValidationResult[Fetch] = (Rules.validHttpUrl(url, InvalidUrl)).map(Fetch)
  }
}
