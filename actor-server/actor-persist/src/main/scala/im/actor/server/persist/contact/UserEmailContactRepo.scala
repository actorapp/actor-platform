package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model

final class UserEmailContactTable(tag: Tag) extends UserContactBase[model.contact.UserEmailContact](tag, "user_email_contacts") with InheritingTable {
  def email = column[String]("email")
  val inherited = UserContactRepo.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, name, isDeleted) <> (model.contact.UserEmailContact.tupled, model.contact.UserEmailContact.unapply)
}

object UserEmailContactRepo {
  val econtacts = TableQuery[UserEmailContactTable]

  def insertOrUpdate(contact: model.contact.UserEmailContact) =
    econtacts.insertOrUpdate(contact)

}