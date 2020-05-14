package $package$

import doobie._
import doobie.implicits._

object UserTable extends Table {
  def initialize: Update0 = sql"""
    DROP TABLE IF EXISTS lords_of_rona_user;
    CREATE TABLE lords_of_rona_user(
      name VARCHAR UNIQUE,
      password VARCHAR,
      id VARCHAR PRIMARY KEY
    )""".update

  def update: Update0 =
    sql"""DROP TABLE IF EXISTS lords_of_rona_user"""
    .update
}
