package im.actor.server.persist

import im.actor.server.models
import slick.driver.PostgresDriver.api._

class UserTable(tag: Tag) extends Table[models.User](tag, "users") {
  import SexColumnType._
  import UserStateColumnType._

  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def name = column[String]("name")
  def countryCode = column[String]("country_code")
  def sex = column[models.Sex]("sex")
  def state = column[models.UserState]("state")

  def * = (id, accessSalt, name, countryCode, sex, state) <> (models.User.tupled, models.User.unapply)
}

object User {
  val users = TableQuery[UserTable]

  def create(user: models.User) =
    users += user
}
