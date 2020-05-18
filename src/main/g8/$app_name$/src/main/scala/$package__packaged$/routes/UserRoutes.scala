package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object UserRoutes extends Routes {
  def routes[F[_]: Sync: Transactor]: HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F] {}
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
}
