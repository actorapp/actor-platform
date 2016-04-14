package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.contact.UserEmailContact
import ContactStatusColumnType._

final class UserEmailContactTable(tag: Tag) extends UserContactBase[UserEmailContact](tag, "user_email_contacts") with InheritingTable {
  def email = column[String]("email")
  val inherited = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, name, isDeleted, status) <> (UserEmailContact.tupled, UserEmailContact.unapply)
}

object UserEmailContactRepo {
  val econtacts = TableQuery[UserEmailContactTable]

  def insertOrUpdate(contact: UserEmailContact) =
    econtacts.insertOrUpdate(contact)

}