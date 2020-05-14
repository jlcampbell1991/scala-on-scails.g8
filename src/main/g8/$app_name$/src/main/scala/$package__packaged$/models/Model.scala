package $package$

import doobie._
import java.util.UUID
import java.time.{LocalDate, Month}

trait Model {
  implicit val uuidGet: Get[UUID] = Get[String].map(UUID.fromString(_))
  implicit val uuidPut: Put[UUID] = Put[String].contramap(_.toString)
}

case class Date(get: LocalDate) {
  override def toString: String = s"$${get.getMonthValue}/$${get.getDayOfMonth}"
}
object Date {
  def apply(month: Int, day: Int, year: Int) = LocalDate.of(year, Month.of(month), day)
  def now: Date = Date(LocalDate.now)
  implicit val get: Get[Date] = Get[LocalDate].map(Date(_))
  implicit val put: Put[Date] = Put[LocalDate].contramap(_.get)
}
