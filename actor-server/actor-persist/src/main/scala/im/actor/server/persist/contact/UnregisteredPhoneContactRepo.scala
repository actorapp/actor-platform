package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.UnregisteredPhoneContact

final class UnregisteredPhoneContactTable(tag: Tag) extends UnregisteredContactBase[UnregisteredPhoneContact](tag, "unregistered_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")
  val inherited = UnregisteredContactRepo.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_phone_contacts_pkey", (phoneNumber, ownerUserId))
  def * = (phoneNumber, ownerUserId, name) <> (UnregisteredPhoneContact.tupled, UnregisteredPhoneContact.unapply)
}

object UnregisteredPhoneContactRepo {
  val phoneContacts = TableQuery[UnregisteredPhoneContactTable]

  def create(phoneNumber: Long, ownerUserId: Int, name: Option[String]) =
    phoneContacts += UnregisteredPhoneContact(phoneNumber, ownerUserId, name)

  def createIfNotExists(phoneNumber: Long, ownerUserId: Int, name: Option[String]) = {
    create(phoneNumber, ownerUserId, name).asTry
  }

  def find(phoneNumber: Long) =
    phoneContacts.filter(_.phoneNumber === phoneNumber).result

  def deleteAll(phoneNumber: Long) =
    phoneContacts.filter(_.phoneNumber === phoneNumber).delete
}
