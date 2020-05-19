package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object UserRoutes extends Routes {
  def routes[F[_]: Sync: Transactor: Http4sDsl]: HttpRoutes[F] =
    publicRoutes <+> authedRoutes

  private def publicRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case GET -> Root / "signup" => Ok(User.add)
      case params @ POST -> Root / "signup" => {
        for {
          form <- params.as[UrlForm]
          user <- User.fromUrlForm(form).flatMap(_.save)
          cookie = Session.cookie(user)
          response <- Ok(User.add).map(_.addCookie(cookie))
        } yield response
      }.handleErrorWith { case e: MalformedMessageBodyFailure => Redirect(User.addUrl) }
    }
  }

  private def authedRoutes[F[_]: Sync: Transactor](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    authedService((userId: UserId) => HttpRoutes.empty)
  }
}
