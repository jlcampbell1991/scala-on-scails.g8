package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object SessionRoutes extends Routes {
  def routes[F[_]: Sync: Transactor: Http4sDsl]: HttpRoutes[F] =
    publicRoutes <+> authedRoutes

  private def publicRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case GET -> Root / "login" => Ok(Session.login.pure[F])
      case params @ POST -> Root / "login" => {
        for {
          form <- params.as[UrlForm]
          session <- Session.fromUrlForm(form)
          user <- session.findUser
          response <- user.fold(Redirect(Session.loginUrl))(_ => Ok(Session.login))
        } yield response
      }.handleErrorWith { case e: MalformedMessageBodyFailure => Redirect(Session.loginUrl) }
      case GET -> Root / "logout" =>
        for {
          response <- Redirect(Session.loginUrl)
        } yield response.removeCookie(Session.COOKIE_NAME)
    }
  }

  private def authedRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    authedService((userId: UserId) => HttpRoutes.empty)
  }
}
