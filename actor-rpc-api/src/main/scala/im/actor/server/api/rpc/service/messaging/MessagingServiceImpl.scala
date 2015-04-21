package im.actor.server.api.rpc.service.messaging

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging._
import im.actor.server.push.SeqUpdatesManagerRegion

class MessagingServiceImpl(implicit
                           val seqUpdManagerRegion: SeqUpdatesManagerRegion,
                           val db: Database,
                           val actorSystem: ActorSystem)
  extends MessagingService with MessagingHandlers with HistoryHandlers
