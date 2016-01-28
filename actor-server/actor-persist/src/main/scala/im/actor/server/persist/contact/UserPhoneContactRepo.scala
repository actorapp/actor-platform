package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.contact.UserPhoneContact

final class UserPhoneContactTable(tag: Tag) extends UserContactBase[UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")
  val inherited = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, name, isDeleted) <> (UserPhoneContact.tupled, UserPhoneContact.unapply)
}

object UserPhoneContactRepo {
  val pcontacts = TableQuery[UserPhoneContactTable]

  def insertOrUpdate(contact: UserPhoneContact) =
    pcontacts.insertOrUpdate(contact)

}