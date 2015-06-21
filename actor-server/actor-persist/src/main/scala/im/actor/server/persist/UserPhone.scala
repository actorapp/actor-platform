package im.actor.server.persist

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

import im.actor.server.models

class UserPhoneTable(tag: Tag) extends Table[models.UserPhone](tag, "user_phones") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
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

  def create(userPhone: models.UserPhone): FixedSqlAction[Int, NoStream, Write] =
    phones += userPhone

  def updateTitle(userId: Int, id: Int, title: String) =
    phones.filter(p ⇒ p.userId === userId && p.id === id).map(_.title).update(title)
}
