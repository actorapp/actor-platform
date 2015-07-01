package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import com.amazonaws.services.s3.transfer.TransferManager
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.session.{ Session, SessionRegion }
import im.actor.server.social.{ SocialManagerRegion, SocialManager }
import im.actor.server.util.FileStorageAdapter

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

trait ImplicitFileStorageAdapter {
  implicit val system: ActorSystem
  implicit val db: Database
  implicit val transferManager: TransferManager

  val testBucket = "actor-uploads-test"

  implicit lazy val fsAdapter: FileStorageAdapter = FileStorageAdapter(testBucket)
}

trait ImplicitServiceDependencies extends ImplicitFileStorageAdapter