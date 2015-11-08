package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model

final class UserPhoneContactTable(tag: Tag) extends UserContactBase[model.contact.UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")
  val inherited = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, name, isDeleted) <> (model.contact.UserPhoneContact.tupled, model.contact.UserPhoneContact.unapply)
}

object UserPhoneContactRepo {
  val pcontacts = TableQuery[UserPhoneContactTable]

  def insertOrUpdate(contact: model.contact.UserPhoneContact) =
    pcontacts.insertOrUpdate(contact)

}