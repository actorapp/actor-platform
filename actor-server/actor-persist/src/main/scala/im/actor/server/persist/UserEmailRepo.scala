package im.actor.server.persist

import im.actor.server.model.UserEmail
import slick.dbio.Effect.{ Write, Read }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

final class UserEmailTable(tag: Tag) extends Table[UserEmail](tag, "user_emails") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def email = column[String]("email")
  def title = column[String]("title")

  def emailUnique = index("idx_user_emails_email", email, unique = true)

  def * = (id, userId, accessSalt, email, title) <> (UserEmail.tupled, UserEmail.unapply)
}

object UserEmailRepo {
  val emails = TableQuery[UserEmailTable]

  val byEmail = Compiled { email: Rep[String] ⇒
    emails.filter(_.email === email)
  }

  val emailExists = Compiled { email: Rep[String] ⇒
    emails.filter(_.email === email).exists
  }

  def findByEmails(emailSet: Set[String]) =
    emails.filter(_.email inSet emailSet).result

  def find(email: String) =
    byEmail(email).result.headOption

  def findByDomain(domain: String) =
    emails.filter(_.email.like(s"%@$domain")).result

  def exists(email: String) =
    emailExists(email).result

  def findByUserId(userId: Int): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.userId === userId).result

  def findByUserIds(userIds: Set[Int]): FixedSqlStreamingAction[Seq[UserEmail], UserEmail, Read] =
    emails.filter(_.userId inSet userIds).result

  def create(id: Int, userId: Int, accessSalt: String, email: String, title: String): FixedSqlAction[Int, NoStream, Write] =
    emails += UserEmail(id, userId, accessSalt, email.toLowerCase, title)
}
