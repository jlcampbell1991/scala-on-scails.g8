package $package$

import cats.effect.Sync
import doobie._
import java.time.{LocalDate, Month}
import java.util.UUID
import org.http4s._
import org.http4s.UrlForm

trait Model {
  implicit val uuidGet: Get[UUID] = Get[String].map(UUID.fromString(_))
  implicit val uuidPut: Put[UUID] = Put[String].contramap(_.toString)

  protected def getValueOrRaiseError[F[_]: Sync](form: UrlForm, value: String): F[String] =
    form.getFirst(value).fold(Sync[F].raiseError[String](MalformedMessageBodyFailure(s"forgot \$value")))(Sync[F].pure)
}

case class Date(get: LocalDate)

object Date extends doobie.util.meta.LegacyLocalDateMetaInstance {
  def apply(month: Int, day: Int, year: Int) = LocalDate.of(year, Month.of(month), day)
  def now: Date = Date(LocalDate.now)
  implicit val get: Get[Date] = Get[LocalDate].map(Date(_))
  implicit val put: Put[Date] = Put[LocalDate].contramap(_.get)
}
