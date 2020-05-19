package $package$

import cats.implicits._
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s._
import org.http4s.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.staticcontent.{resourceService, ResourceService}
import scala.concurrent.ExecutionContext.global
import org.http4s.server._
import doobie._

object SetupServer {
  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F], Xa: Transactor[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      assetsRoutes = resourceService[F](ResourceService.Config[F]("", Blocker.liftExecutionContext(global)))
      finalHttpApp = Logger.httpApp(true, true)(Routes.routes[F](assetsRoutes).orNotFound)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
