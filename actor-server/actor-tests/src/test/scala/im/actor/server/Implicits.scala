package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import com.amazonaws.auth.{ EnvironmentVariableCredentialsProvider, AWSCredentialsProvider }
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.ActorSpecHelpers
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.session.{ Session, SessionRegion }
import im.actor.server.social.{ SocialManagerRegion, SocialManager }
import im.actor.server.util.{ S3StorageAdapterConfig, S3StorageAdapter, FileStorageAdapter }

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

  implicit lazy val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit lazy val fsAdapterS3: S3StorageAdapter = new S3StorageAdapter(S3StorageAdapterConfig.load.get)
  implicit lazy val fsAdapter: FileStorageAdapter = fsAdapterS3
}

trait ImplicitServiceDependencies extends ImplicitFileStorageAdapter