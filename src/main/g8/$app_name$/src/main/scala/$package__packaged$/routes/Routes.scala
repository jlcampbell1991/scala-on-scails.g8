package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.http4s.headers._
import org.http4s.headers.Location
import org.http4s.HttpRoutes
import org.http4s.Uri._
import doobie._

trait Routes {
  def Redirect(uri: String): F[Response[F]] =
    SeeOther(Location(Uri(authority = Some(Authority(host = RegName(uri))))))

  def ifAuthed[F[_]: Sync: Http4sDsl](maybeId: Option[UserId])(f: UserId => F[Response[F]]): F[Response[F]] = {
    maybeId match {
      case None => Redirect(Session.loginUrl)
      case Some(id) => f(id)
    }
  }
}

object Routes {
  def routes[F[_]: Sync : Transactor](M: AuthMiddleware[F, Option[UserId]]): HttpRoutes[F] = {
    UserRoutes.routes[F] <+>
    SessionRoutes.routes[F]
  }
}
