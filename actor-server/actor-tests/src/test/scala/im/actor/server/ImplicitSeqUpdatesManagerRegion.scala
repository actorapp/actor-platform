package im.actor.server

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.push.SeqUpdatesManagerRegion

trait ImplicitSeqUpdatesManagerRegion extends ActorSpecHelpers {
  protected implicit val system: ActorSystem
  protected implicit val db: Database

  protected implicit lazy val seqUpdManagerRegion: SeqUpdatesManagerRegion = buildSeqUpdManagerRegion()
}
