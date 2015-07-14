package im.actor.server.persist

import slick.dbio.Effect.{ Write, Read }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

import im.actor.server.models

class UserEmailTable(tag: Tag) extends Table[models.UserEmail](tag, "user_emails") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def email = column[String]("email")
  def title = column[String]("title")

  def emailUnique = index("idx_user_emails_email", email, unique = true)

  def * = (id, userId, accessSalt, email, title) <> (models.UserEmail.tupled, models.UserEmail.unapply)
}

object UserEmail {
  val emails = TableQuery[UserEmailTable]

  def byEmail(email: String) =
    emails.filter(_.email === email)

  def findByEmails(emailSet: Set[String]) =
    emails.filter(_.email inSet emailSet).result

  def find(email: String) =
    byEmail(email).result.headOption

  def exists(email: String) =
    byEmail(email).exists.result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[models.UserEmail], models.UserEmail, Read] =
    emails.filter(_.userId === userId).result

  def findByUserIds(userIds: Set[Int]): FixedSqlStreamingAction[Seq[models.UserEmail], models.UserEmail, Read] =
    emails.filter(_.userId inSet userIds).result

  def create(id: Int, userId: Int, accessSalt: String, email: String, title: String): FixedSqlAction[Int, NoStream, Write] =
    emails += models.UserEmail(id, userId, accessSalt, email, title)

  def create(email: models.UserEmail): FixedSqlAction[Int, NoStream, Write] =
    emails += email

}
