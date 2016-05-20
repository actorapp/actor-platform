package im.actor.server.persist.social

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.social.RelationStatus

object RelationStatusColumnType {
  implicit val relationStatusCT = MappedColumnType.base[RelationStatus, Int](_.intValue, RelationStatus.apply)
}