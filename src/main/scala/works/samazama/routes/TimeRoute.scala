package works.samazama.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import works.samazama.api.{Dto, V1}
import works.samazama.directives.ApiDirectives

object TimeRoute extends ApiDirectives {

  def route: Route = sinceApi(V1) {
    setupResource("time") {
      pathEnd {
        import works.samazama.api.V1._
        get {
          complete((StatusCodes.OK, Dto.EpochTime(epochMillis = System.currentTimeMillis)))
        }
      }
    }
  }
}
