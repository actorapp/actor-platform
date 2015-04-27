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

  def setCountryCode(userId: Int, countryCode: String) =
    users.filter(_.id === userId).map(_.countryCode).update(countryCode)

  def find(id: Int) =
    users.filter(_.id === id).result

  def findName(id: Int) =
    users.filter(_.id === id).map(_.name).result.headOption

  // TODO: #perf will it create prepared statement for each ids length?
  def findSalts(ids: Set[Int]) =
    users.filter(_.id inSet ids).map(u ⇒ (u.id, u.accessSalt)).result

  def findByIds(ids: Set[Int]) =
    users.filter(_.id inSet ids).result
}
