package im.actor.server.persist

import im.actor.server.models
import slick.driver.PostgresDriver.api._
import Database.dynamicSession

class UserPhoneTable(tag: Tag) extends Table[models.UserPhone](tag, "user_phones") {
  def id = column[Int]("id", O.PrimaryKey)
  def userId = column[Int]("user_id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def number = column[Long]("number")
  def title = column[String]("title")

  def * = (id, userId, accessSalt, number, title) <> (models.UserPhone.tupled, models.UserPhone.unapply)
}

object UserPhone {
  val phones = TableQuery[UserPhoneTable]

  def byPhoneNumber(number: Long) = phones.filter(_.number === number)

  def exists(number: Long) = byPhoneNumber(number).exists.result

  // TODO: rename to findByNumber
  def findByPhoneNumber(number: Long) = byPhoneNumber(number).result

  def findByNumbers(numbers: Set[Long]) =
    phones.filter(_.number inSet numbers).result

  def findByUserId(userId: Int) = phones.filter(_.userId === userId).result

  def create(id: Int, userId: Int, accessSalt: String, number: Long, title: String) =
    phones += models.UserPhone(id, userId, accessSalt, number, title)
}
