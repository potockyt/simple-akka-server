package works.samazama.routes

import java.io.{BufferedInputStream, InputStream}

import akka.http.scaladsl.model.HttpEntity.{Chunk, ChunkStreamPart, LastChunk}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import works.samazama.api.{EmptyResponse, V1}
import works.samazama.config.AppConfig
import works.samazama.directives.ApiDirectives

import scala.collection.{AbstractIterator, Iterator}
import scala.util.Try

object FileRoute extends ApiDirectives {
  private val defaultBufferSize: Int = 1024
  private val file = "/shakespeare.txt"

  def route: Route = sinceApi(V1) {
    setupResource("file") {
      pathEnd {
        get {
          parameters("limit".as[Long].?) { limitOpt =>
            val limit = limitOpt.getOrElse(AppConfig.akka.maxContentLength)
            if (limit < 0) {
              complete(EmptyResponse(StatusCodes.BadRequest))
            } else {
              val is: InputStream = new BufferedInputStream(getClass.getResourceAsStream(file))
              // Chunked encoding allows to send contents of the file
              // without buffering it whole/up-to-limit for the response
              complete(
                HttpResponse(
                  entity = HttpEntity.Chunked(
                    ContentTypes.`application/octet-stream`,
                    Source.fromIterator(() => fileIterator(is, limit)).recover {
                      case _: Exception =>
                        Try { is.close() }
                        LastChunk
                    }
                  )
                )
              )
            }
          }
        }
      }
    }
  }

  private def fileIterator(fileInputStream: InputStream, limit: Long): Iterator[ChunkStreamPart] = {
    require(limit > 0)

    new AbstractIterator[ChunkStreamPart] {
      private val bufferSize: Int = if (limit > defaultBufferSize) defaultBufferSize else limit.toInt
      private val buffer = Array.ofDim[Byte](bufferSize)
      private var counter = 0L
      private var more = true

      def hasNext: Boolean = more

      def next: ChunkStreamPart = {
        if (counter >= limit) {
          more = false
          LastChunk
        } else {
          val readSize: Int = if (limit - counter >= bufferSize) bufferSize else (limit - counter).toInt
          val len = fileInputStream.read(buffer, 0, readSize)
          counter += len
          if (len < 0) {
            more = false
            LastChunk
          } else if (len < bufferSize) {
            Chunk(Array.copyOf(buffer, len))
          } else {
            Chunk(buffer)
          }
        }
      }
    }
  }
}
