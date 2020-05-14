package $package$

import cats.implicits._, cats.data._
import cats.effect.{ConcurrentEffect, ContextShift, Timer, Blocker}
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
      authUser = Kleisli((r: Request[F]) => OptionT.liftF(Session.isLoggedIn(r.headers).pure[F]))
      middleware = AuthMiddleware(authUser)
      finalHttpApp = Logger.httpApp(true, true)(Routes.routes[F](middleware, assetsRoutes).orNotFound)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
