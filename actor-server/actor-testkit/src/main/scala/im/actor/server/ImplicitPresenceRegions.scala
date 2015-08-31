package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }

trait ImplicitPresenceRegions {
  protected implicit val system: ActorSystem

  implicit lazy val presenceManagerRegion = PresenceManager.startRegion()
  implicit lazy val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
}