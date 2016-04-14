package im.actor.server.persist.contact

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.contact.ContactStatus

object ContactStatusColumnType {
  implicit val actorColumnType = MappedColumnType.base[ContactStatus, Int](_.intValue, ContactStatus.apply)
}