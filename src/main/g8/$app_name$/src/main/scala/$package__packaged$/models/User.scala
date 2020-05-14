package $package$

import cats.implicits._
import cats.effect.Sync
import org.http4s.UrlForm
import doobie._
import doobie.implicits._
import com.github.t3hnar.bcrypt._
import java.util.UUID

final case class UserId(id: UUID) {
  override def toString = id.toString
}
object UserId {
  def apply(id: String): UserId = UserId(UUID.fromString(id))
  def random: UserId = UserId(UUID.randomUUID)
}

final case class Password(get: String)
object Password {
  def encrypt(p: String): Password = Password(p.bcrypt)
}

final case class User(name: String, unencPass: Password, userId: UserId) {
  def id: String = userId.toString
  def password: String = unencPass.get
  def isEmpty: Boolean = name == "" && password == ""

  def save[F[_]: Sync : Transactor] =
    if(this.isEmpty) this.pure[F]
    else User.create[F](this)

  def update[F[_]: Sync : Transactor] =
    User.update[F](this)

  def destroy[F[_]: Sync : Transactor] =
    User.destroy[F](this)
}
object User extends Model {
  def apply[F[_]: Sync : Transactor](form: UrlForm): Option[F[User]] = {
    for {
      name <- form.get("name")
      password <- form.get("password")
      passwordConfirm <- form.get("passwordConfirmation")
    } yield {
      if(password == passwordConfirm) User(name, Password.encrypt(password), UserId.random)
      else User.empty
    }
  }.headOption.map(_.save[F])

  def empty: User =
    User("",Password(""), UserId.random)

  def find[F[_]: Sync](id: UserId)(implicit XA: Transactor[F]): F[User] =
    sql"""select * from $app_name;format="snake"$_user where id = \${id.toString}"""
      .query[User].unique.transact(XA)

  def find[F[_]: Sync](name: String)(implicit XA: Transactor[F]): F[User] =
    sql"""select * from $app_name;format="snake"$_user where name = \${name}"""
      .query[User].unique.transact(XA)

  def create[F[_]: Sync](user: User)(implicit XA: Transactor[F]): F[User] = {
    sql"""
    insert into $app_name;format="snake"$_user (name, password, id)
    values
    (
      \${user.name},
      \${user.password},
      \${user.id}
    )
    """
    .update.withUniqueGeneratedKeys[User]("name", "password", "id").transact(XA)
  }

  def update[F[_]: Sync](user: User): Update0 =
    sql"""
      update user set
        name = \${user.name},
        password = \${user.password}
      where id = \${user.id}
      """
      .update

  def destroy[F[_]: Sync](user: User): Update0 =
    sql"""delete from user where id = \${user.id}"""
    .update

  def add = views.html.user.signup()
  def addUrl = "/signup"
}
