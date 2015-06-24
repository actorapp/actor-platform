package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class UnregisteredEmailContactTable(tag: Tag) extends UnregisteredContactBase[models.UnregisteredEmailContact](tag, "unregistered_email_contacts") with InheritingTable {
  def email = column[String]("email")
  val inherited = UnregisteredContact.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_email_contacts_pkey", (email, ownerUserId))
  def * = (email, ownerUserId, name) <> (models.UnregisteredEmailContact.tupled, models.UnregisteredEmailContact.unapply)
}

object UnregisteredEmailContact {
  val emailContacts = TableQuery[UnregisteredEmailContactTable]

  def create(email: String, ownerUserId: Int, name: Option[String]) =
    emailContacts += models.UnregisteredEmailContact(email, ownerUserId, name)

  def create(contacts: Seq[models.UnregisteredEmailContact]) =
    emailContacts ++= contacts

  def createIfNotExists(email: String, ownerUserId: Int, name: Option[String]) = {
    create(email, ownerUserId, name).asTry
  }

  def find(email: String) =
    emailContacts.filter(_.email === email).result

  def deleteAll(email: String) =
    emailContacts.filter(_.email === email).delete
}