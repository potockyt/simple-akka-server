package works.samazama.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{AsyncFlatSpec, Matchers}
import works.samazama.api.Dto
import io.circe.generic.auto._

class TimeRouteSpec extends AsyncFlatSpec with ScalatestRouteTest with Matchers with RouteSpecSupport {
  val timeRoute = "/api/time"

  "Get time" should "return epoch time in milliseconds" in {
    val currentTimeMillis = System.currentTimeMillis()
    Get(timeRoute).withHeaders(defaultApiHeaders) ~> AllRoutes() ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe contentTypeVN
      assert(entityAs[Dto.EpochTime].epochMillis > currentTimeMillis)
    }
  }

}
