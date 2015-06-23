package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class UnregisteredPhoneContactTable(tag: Tag) extends UnregisteredContactBase[models.UnregisteredPhoneContact](tag, "unregistered_phone_contacts") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")
  val inherited = UnregisteredContact.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_phone_contacts_pkey", (phoneNumber, ownerUserId))
  def * = (phoneNumber, ownerUserId, name) <> (models.UnregisteredPhoneContact.tupled, models.UnregisteredPhoneContact.unapply)
}

object UnregisteredPhoneContact {
  val phoneContacts = TableQuery[UnregisteredPhoneContactTable]

  def create(phoneNumber: Long, ownerUserId: Int, name: Option[String]) =
    phoneContacts += models.UnregisteredPhoneContact(phoneNumber, ownerUserId, name)

  def createIfNotExists(phoneNumber: Long, ownerUserId: Int, name: Option[String]) = {
    create(phoneNumber, ownerUserId, name).asTry
  }

  def find(phoneNumber: Long) =
    phoneContacts.filter(_.phoneNumber === phoneNumber).result

  def deleteAll(phoneNumber: Long) =
    phoneContacts.filter(_.phoneNumber === phoneNumber).delete
}
