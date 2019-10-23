package works.samazama.routes

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{AsyncFlatSpec, Matchers}
import works.samazama.api.Dto

import scala.util.{Failure, Success}

class FetchRouteSpec extends AsyncFlatSpec with ScalatestRouteTest with Matchers with RouteSpecSupport with Eventually with IntegrationPatience {
  val fetchRoute = "/api/fetch"
  val resourceSize = 94630 // shakespeare.txt
  val fetch = Dto.Fetch(url = "https://raw.githubusercontent.com/potockyt/simple-akka-server/master/src/main/resources/shakespeare.txt")

  "Fetch of github resource" should "return whole resource" in {
    Post(fetchRoute, fetch.asJson).withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
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
        assert(responseSize == resourceSize)
      }
    }
  }


  "Contents of resource" should "be THE SONNETS" in {
    Post(fetchRoute, fetch.asJson).withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
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
        assert(responseString.substring(0, 11) == "THE SONNETS")
      }
    }
  }

  "Fetch with incorrect url" should "return fail with validation error" in {
    val fetch = Dto.Fetch(url = "ftp://localhost/shakespeare.txt")
    case class Error(error: String)
    Post(fetchRoute, fetch.asJson).withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
      status shouldBe StatusCodes.BadRequest
      contentType shouldBe ContentTypes.`application/json`
      assert(entityAs[Error].error == "[url] URL is not valid")
    }
  }
}
