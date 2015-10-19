package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

private[contact] abstract class UnregisteredContactBase[T](tag: Tag, tname: String) extends Table[T](tag, tname) {
  def ownerUserId = column[Int]("owner_user_id", O.PrimaryKey)
  def name = column[Option[String]]("name")
}

final class UnregisteredContactTable(tag: Tag) extends UnregisteredContactBase[models.UnregisteredContact](tag, "unregistered_contacts") {
  def * = (ownerUserId, name) <> (models.UnregisteredContact.tupled, models.UnregisteredContact.unapply)
}

object UnregisteredContactRepo {
  val ucontacts = TableQuery[UnregisteredContactTable]
}