package works.samazama

import works.samazama.api.RouteResponse

abstract class AppError(val message: String, val cause: Option[Throwable] = None) extends RouteResponse

case class UnexpectedError(msg: String, exception: Throwable) extends AppError(msg, Some(exception))

case class ValidationError(msg: String) extends AppError(msg)
