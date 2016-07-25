package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.UnregisteredEmailContact

final class UnregisteredEmailContactTable(tag: Tag) extends UnregisteredContactBase[UnregisteredEmailContact](tag, "unregistered_email_contacts") with InheritingTable {
  def email = column[String]("email")
  val inherited = UnregisteredContactRepo.ucontacts.baseTableRow

  def pk = primaryKey("unregistered_email_contacts_pkey", (email, ownerUserId))
  def * = (email, ownerUserId, name) <> (UnregisteredEmailContact.tupled, UnregisteredEmailContact.unapply)
}

object UnregisteredEmailContactRepo {
  private val emailContacts = TableQuery[UnregisteredEmailContactTable]

  private def create(email: String, ownerUserId: Int, name: Option[String]) =
    emailContacts += UnregisteredEmailContact(email, ownerUserId, name)

  def createIfNotExists(email: String, ownerUserId: Int, name: Option[String]) = {
    create(email, ownerUserId, name).asTry
  }

  def find(email: String) =
    emailContacts.filter(_.email === email).result

  def deleteAll(email: String) =
    emailContacts.filter(_.email === email).delete
}
