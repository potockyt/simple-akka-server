package works.samazama

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import works.samazama.config.AppConfig
import works.samazama.routes.{FetchRoute, FileRoute, TimeRoute}

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object Boot extends App with RouteConcatenation {
  private implicit val system: ActorSystem = ActorSystem("boot-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val log = Logging(system, getClass)

  Try {
    (AppConfig.http.interface, AppConfig.http.port)
  } match {
    case Success((interface, port)) =>
      val route = Route.seal {
        TimeRoute.route ~
        FileRoute.route ~
        FetchRoute.route
      }

      val bindHttp = Http().bindAndHandle(handler = route, interface = interface, port = port)

      bindHttp.onComplete {
        case Success(value) =>
          log.info("{}", value)
          log.info("Press any key to stop the server")
          StdIn.readLine()
          bindHttp
            .flatMap(_.unbind())
            .onComplete(_ => system.terminate())
        case Failure(ex) =>
          log.error("Failed to bind {} at port {}, {}", interface, port, ex.getMessage)
          system.terminate()
      }

    case Failure(ex) =>
      log.error("Failed to read config, {}", ex.getMessage)
      system.terminate()
  }

}
