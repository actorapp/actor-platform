package im.actor.server.persist

import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._
import slick.profile.FixedSqlStreamingAction

import im.actor.server.models

class UserEmailTable(tag: Tag) extends Table[models.UserEmail](tag, "user_emails") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def email = column[String]("email")
  def title = column[String]("title")

  def * = (id, userId, accessSalt, email, title) <> (models.UserEmail.tupled, models.UserEmail.unapply)
}

object UserEmail {
  val emails = TableQuery[UserEmailTable]

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[models.UserEmail], models.UserEmail, Read] =
    emails.filter(_.userId === userId).result

  def findByUserIds(userIds: Set[Int]): FixedSqlStreamingAction[Seq[models.UserEmail], models.UserEmail, Read] =
    emails.filter(_.userId inSet userIds).result
}
