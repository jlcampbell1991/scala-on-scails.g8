package $package$

import cats.effect.Sync
import org.http4s._
import org.http4s.{UrlForm, Headers, ResponseCookie}
import doobie._
import doobie.implicits._
import com.github.t3hnar.bcrypt._
import org.reactormonk.{CryptoBits, PrivateKey}
import java.time._

case class Session(username: String, password: String) extends Model {
  def findUser[F[_]: Sync](implicit XA: Transactor[F]): F[Option[User]] =
    sql"""select * from lords_of_rona_user where name = \${username}"""
    .query[User].option.transact(XA)

  def auth[F[_]: Sync](user: User): User =
    if(password.isBcrypted(user.password)) user
    else User.empty
}
object Session {
  def apply(form: UrlForm): Option[Session] = {
    for {
      name <- form.get("username")
      password <- form.get("password")
    } yield Session(name, password)
  }.headOption

  val COOKIE_NAME = "$app_name$_cookie"
  private val key = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  private val crypto = CryptoBits(key)
  def cookie(user: User): ResponseCookie =
    ResponseCookie(
      name = COOKIE_NAME,
      content = crypto.signToken(user.id, Instant.now.getEpochSecond.toString))

  def requestCookie(user: User): RequestCookie =
    RequestCookie(
      name = COOKIE_NAME,
      content = crypto.signToken(user.id, Instant.now.getEpochSecond.toString))

  def isLoggedIn(requestHeaders: Headers): Option[UserId] =
    for {
      header <- headers.Cookie.from(requestHeaders)
      cookie <- header.values.toList.find(_.name == COOKIE_NAME)
      token <- crypto.validateSignedToken(cookie.content)
    } yield UserId(token)

  def login = views.html.session.login()
  def loginUrl = "/login"
}
