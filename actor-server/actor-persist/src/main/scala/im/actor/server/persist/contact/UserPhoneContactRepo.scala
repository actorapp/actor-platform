package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

final class UserPhoneContactTable(tag: Tag) extends UserContactBase[models.contact.UserPhoneContact](tag, "user_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")
  val inherited = UserContactRepo.contacts.baseTableRow

  def * = (phoneNumber, ownerUserId, contactUserId, name, isDeleted) <> (models.contact.UserPhoneContact.tupled, models.contact.UserPhoneContact.unapply)
}

object UserPhoneContactRepo {
  val pcontacts = TableQuery[UserPhoneContactTable]

  def insertOrUpdate(contact: models.contact.UserPhoneContact) =
    pcontacts.insertOrUpdate(contact)

}