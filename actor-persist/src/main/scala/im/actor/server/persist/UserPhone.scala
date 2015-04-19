package im.actor.server.persist

import slick.dbio.Effect.{ Write, Read }
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

import im.actor.server.models
import slick.driver.PostgresDriver.api._

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

  def findByNumbers(numbers: Set[Long]): FixedSqlStreamingAction[Seq[models.UserPhone], models.UserPhone, Read] =
    phones.filter(_.number inSet numbers).result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[models.UserPhone], models.UserPhone, Read] =
    phones.filter(_.userId === userId).result

  def findByUserIds(userIds: Set[Int]): FixedSqlStreamingAction[Seq[models.UserPhone], models.UserPhone, Read] =
    phones.filter(_.userId inSet userIds).result

  def create(id: Int, userId: Int, accessSalt: String, number: Long, title: String): FixedSqlAction[Int, NoStream, Write] =
    phones += models.UserPhone(id, userId, accessSalt, number, title)
}
