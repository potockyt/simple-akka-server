package works.samazama.routes

import akka.http.scaladsl.server.{Route, RouteConcatenation}

object AllRoutes extends RouteConcatenation {

  def apply(): Route = concat(
    TimeRoute.route,
    FileRoute.route,
    FetchRoute.route
  )

}
