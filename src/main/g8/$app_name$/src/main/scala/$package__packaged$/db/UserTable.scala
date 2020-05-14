package $package$

import doobie._
import doobie.implicits._

object UserTable extends Table {
  def initialize: Update0 = sql"""
    DROP TABLE IF EXISTS $app_name;format="snake"$_user;
    CREATE TABLE $app_name;format="snake"$_user(
      name VARCHAR UNIQUE,
      password VARCHAR,
      id VARCHAR PRIMARY KEY
    )""".update

  def update: Update0 =
    sql"""DROP TABLE IF EXISTS $app_name;format="snake"$_user"""
    .update
}
