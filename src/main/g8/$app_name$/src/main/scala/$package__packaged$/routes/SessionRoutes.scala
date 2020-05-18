package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object SessionRoutes extends Routes {
  def routes[F[_]: Sync: Transactor]: HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F] {}
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
}
