package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object SessionRoutes extends Routes {
  def routes[F[_]: Sync : Transactor]: HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of {
      case GET -> Root / "login" => Ok(Session.login.pure[F])
      case params @ POST -> Root / "login" =>
        for {
          fUser <- params.as[UrlForm]
            .map(Session(_)
            .map(session => session.findUser
            .map(_.map(session.auth(_))
            .getOrElse(User.empty)))
            .getOrElse(User.empty.pure[F]))
          user <- fUser
          response <- if(user.isEmpty) Redirect(Session.loginUrl)
                      else Ok(Session.login)
        } yield {
          if(user.isEmpty) response
          else response.addCookie(Session.cookie(user))
        }
      case GET -> Root / "logout" =>
        for {
          response <- Redirect(Session.loginUrl)
        } yield response.removeCookie(Session.COOKIE_NAME)
    }
  }
}
