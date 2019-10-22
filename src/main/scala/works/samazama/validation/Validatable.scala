package works.samazama.validation

import cats.data.Validated.Invalid
import cats.data._

abstract class ValidationFailure(val field: String, val message: String)

trait Validatable[A] {

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  def validate: ValidationResult[A]
}

// IllegalArgumentException interprets as an invalid value within the payload
// and causes a ValidationRejection instead of MalformedRequestContentRejection.
case class ValidationException(invalidArgs: Invalid[NonEmptyList[ValidationFailure]]) extends IllegalArgumentException
