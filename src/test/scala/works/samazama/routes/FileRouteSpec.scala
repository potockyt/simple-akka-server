package works.samazama.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}

import scala.util.{Failure, Success}

class FileRouteSpec extends AsyncFlatSpec with ScalatestRouteTest with Matchers with RouteSpecSupport with Eventually with IntegrationPatience {
  val fileRoute = "/api/file"
  val fileSize = 94630 // /shakespeare.txt

  "Get file" should "return whole file" in {
    Get(fileRoute).withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/octet-stream`
      assert(responseEntity.isChunked)
      val future = responseEntity.dataBytes.runFold(0L)((size, bs) => size + bs.size)
      var responseSize = 0L
      future.onComplete {
        case Success(size) => responseSize = size
        case Failure(ex) => throw ex
      }
      eventually {
        assert(responseSize == fileSize)
      }
    }
  }


  "Get file with limit N" should "return first N bytes of file" in {
    val limit = 1024
    Get(s"$fileRoute?limit=$limit").withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/octet-stream`
      assert(responseEntity.isChunked)
      val future = responseEntity.dataBytes.runFold(0L)((size, bs) => size + bs.size)
      var responseSize = 0L
      future.onComplete {
        case Success(size) => responseSize = size
        case Failure(ex) => throw ex
      }
      eventually {
        assert(responseSize == limit)
      }
    }
  }

  "Get file with limit 11" should "return first 11 bytes: THE SONNETS" in {
    val limit = 11
    Get(s"$fileRoute?limit=$limit").withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/octet-stream`
      assert(responseEntity.isChunked)
      val future = responseEntity.dataBytes.runFold("")((str, bs) => str + bs.utf8String)
      var responseString = ""
      future.onComplete {
        case Success(str) => responseString = str
        case Failure(ex) => throw ex
      }
      eventually {
        assert(responseString == "THE SONNETS")
      }
    }
  }
}
