package im.actor.server

import akka.actor.{ ActorRef, ActorSystem }
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
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
  implicit val awsCredentials: AWSCredentialsProvider

  lazy val s3BucketName = "actor-uploads-test"

  implicit lazy val transferManager: TransferManager = new TransferManager(awsCredentials)
  implicit lazy val s3ScalaClient: AmazonS3ScalaClient = new AmazonS3ScalaClient(awsCredentials)

  implicit lazy val fsAdapter: FileStorageAdapter = FileStorageAdapter(s3BucketName)
}

trait ImplicitServiceDependencies extends ImplicitFileStorageAdapter