package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class UserEmailContactTable(tag: Tag) extends UserContactBase[models.contact.UserEmailContact](tag, "user_email_contacts") with InheritingTable {
  def email = column[String]("email")
  val inherited = UserContact.contacts.baseTableRow

  def * = (email, ownerUserId, contactUserId, name, accessSalt, isDeleted) <> (models.contact.UserEmailContact.tupled, models.contact.UserEmailContact.unapply)
}

object UserEmailContact {
  val econtacts = TableQuery[UserEmailContactTable]

  def insertOrUpdate(contact: models.contact.UserEmailContact) =
    econtacts.insertOrUpdate(contact)

}