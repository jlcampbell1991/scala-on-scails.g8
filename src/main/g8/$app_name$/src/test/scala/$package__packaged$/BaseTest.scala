package $package$

import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.server._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

abstract class BaseTest extends FreeSpec with Matchers with ScalaCheckPropertyChecks with TypeCheckedTripleEquals {
  import DBDriver._

  val AssetsRoutes: HttpRoutes[IO] = HttpRoutes.empty

  val M: AuthMiddleware[IO, Option[UserId]] = AuthMiddleware(
    Kleisli((r: Request[IO]) => OptionT.liftF(Session.isLoggedIn(r.headers).pure[IO])))

  val service: HttpRoutes[IO] = Routes.routes(M, AssetsRoutes)

  def check[A](actual: IO[Response[IO]], expectedStatus: org.http4s.Status, expectedBody: Option[A])(
    implicit ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp = actual.unsafeRunSync
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody match {
      case Some(_) =>
        expectedBody.fold[Boolean](actualResp.body.compile.toVector.unsafeRunSync.isEmpty)(expected =>
          actualResp.as[A].unsafeRunSync == expected
        )
      case None => true
    }
    statusCheck && bodyCheck
  }
}
