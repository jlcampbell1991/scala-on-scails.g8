package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.twirl._
import doobie._

object UserRoutes extends Routes {
  def routes[F[_]: Sync : Transactor]: HttpRoutes[F] = {
    implicit val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of {
      case GET -> Root / "signup" => Ok(User.add)
      case params @ POST -> Root / "signup" => {
        for {
          fUser <- params.as[UrlForm].map(User(_).getOrElse(User.empty.pure[F]))
          user <- fUser
          cookie <- Session.cookie(user).pure[F]
          response <- user match {
            case _ if(user.isEmpty) => Ok(User.add)
            case _ => Redirect(User.addUrl)
          }
       } yield response.addCookie(cookie)
      }
    }
  }
}
