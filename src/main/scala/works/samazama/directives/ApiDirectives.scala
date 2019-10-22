package works.samazama.directives

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import cats.data.Validated.{Invalid, Valid}
import io.circe.Encoder
import works.samazama.api.ApiVersion
import works.samazama.config.AppConfig
import works.samazama.validation.{Validatable, ValidationException}
import works.samazama.{AppError, UnexpectedError, ValidationError}

import scala.concurrent.Future

trait ApiDirectives extends Directives {
  private val errorEncoder: Encoder[AppError] = Encoder.forProduct1("error") { error: AppError =>
    error.message
  }

  protected val apiExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Exception => complete(UnexpectedError(s"Unexpected API exception", e))
  }

  protected val apiRejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case ValidationRejection(_, Some(cause @ ValidationException(_))) =>
        extractRequest { request =>
          extractMaterializer { implicit mat =>
            request.discardEntityBytes()
            complete(
              ValidationError(cause.invalidArgs.e.collect { case i => s"[${i.field}] ${i.message}" }.mkString(","))
            )
          }
        }
    }
    .result()

  def sinceApi(version: ApiVersion): Directive0 = mapInnerRoute { inner =>
    headerValueByType[Accept]((): Unit) { accept =>
      rejectUnsupportedMediaType(accept, version) match {
        case Some(rejection) =>
          reject(rejection)
        case _ =>
          inner
      }
    }
  }

  def setupResource(resourcePath: PathMatcher[Unit]): Directive0 =
    pathPrefix(AppConfig.http.apiPath / resourcePath) &
    redirectToNoTrailingSlashIfPresent(StatusCodes.Found) &
    handleExceptions(apiExceptionHandler) &
    handleRejections(apiRejectionHandler)

  implicit final def appErrorToHttpResponse(error: AppError): ToResponseMarshallable = {
    val httpEntity = HttpEntity(MediaTypes.`application/json`, errorEncoder.apply(error).noSpaces)
    error match {
      case _: ValidationError =>
        HttpResponse(status = StatusCodes.BadRequest, entity = httpEntity)
      case _ =>
        HttpResponse(status = StatusCodes.InternalServerError, entity = httpEntity)
    }
  }

  /**
    * Validates entity after unmarshalling and if successful, returns valid entity
    * otherwise returns validation exception
    */
  final implicit def validatedEntityUnmarshaller[A <: Validatable[A]](
    implicit um: FromRequestUnmarshaller[A]
  ): FromRequestUnmarshaller[Valid[A]] =
    um.flatMap { _ => _ => entity =>
      entity.validate match {
        case v @ Valid(_) =>
          Future.successful(v)
        case failures @ Invalid(_) =>
          Future.failed(ValidationException(failures))
      }
    }

  private def rejectUnsupportedMediaType(accept: Accept, version: ApiVersion): Option[Rejection] = {
    val mediaTypeMatches = for {
      r <- accept.mediaRanges.filter(mediaRange => mediaRange.isApplication || mediaRange.isWildcard)
      v <- version.mediaTypes ++ List(MediaTypes.`application/octet-stream`)
    } yield r.matches(v)

    mediaTypeMatches match {
      case Nil =>
        None
      case list @ _ if list.contains(true) =>
        None
      case _ =>
        Some(
          UnacceptedResponseContentTypeRejection(
            version.mediaTypes.map(mt => ContentNegotiator.Alternative(mt)).toSet
          )
        )
    }
  }
}
