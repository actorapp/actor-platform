package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.session.{ Session, SessionRegion }
import im.actor.server.social.{ SocialManagerRegion, SocialManager }

trait ImplicitSeqUpdatesManagerRegion extends ActorSpecHelpers {
  implicit val system: ActorSystem
  implicit val db: Database

  implicit lazy val seqUpdManagerRegion: SeqUpdatesManagerRegion = buildSeqUpdManagerRegion()
}

trait ImplicitSocialManagerRegion {
  implicit val system: ActorSystem
  implicit val db: Database

  implicit lazy val socialManagerRegion: SocialManagerRegion = SocialManager.startRegion()
}

trait ImplicitSessionRegionProxy {
  implicit val system: ActorSystem

  val mediator: ActorRef

  implicit lazy val sessionRegion: SessionRegion = Session.startRegionProxy()
}

trait ImplicitRegions
  extends ImplicitSeqUpdatesManagerRegion
  with ImplicitSessionRegionProxy
  with ImplicitSocialManagerRegion