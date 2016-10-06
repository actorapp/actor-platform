package im.actor.server.push.google

import akka.actor._
import akka.event.Logging
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import cats.data.Xor
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.server.db.DbExtension
import im.actor.server.model.push.GCMPushCredentials
import im.actor.server.persist.push.GooglePushCredentialsRepo
import io.circe.generic.auto._
import io.circe.jawn._
import io.circe.syntax._
import spray.client.pipelining._
import spray.http.HttpHeaders.Authorization
import spray.http._

import scala.concurrent.{ Future, TimeoutException }
import scala.util.{ Failure, Success, Try }

private final case class GooglePushKey(projectId: Long, key: String)

private object GooglePushKey {
  def load(config: Config): Try[GooglePushKey] = {
    for {
      projectId ← config.get[Try[Long]]("project-id")
      key ← config.get[Try[String]]("key")
    } yield GooglePushKey(projectId, key)
  }
}

private final case class GooglePushManagerConfig(keys: List[GooglePushKey])

private object GooglePushManagerConfig {
  def load(googlePushConfig: Config): Try[GooglePushManagerConfig] =
    for {
      keyConfigs ← googlePushConfig.get[Try[List[Config]]]("keys")
      keys ← Try(keyConfigs map (GooglePushKey.load(_).get))
    } yield GooglePushManagerConfig(keys)
}

final case class GooglePushMessage(
  to:           String,
  collapse_key: Option[String],
  data:         Option[Map[String, String]],
  time_to_live: Option[Int]
)

object GooglePushExtension extends ExtensionId[GooglePushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): GooglePushExtension = new GooglePushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = GooglePushExtension
}

final class GooglePushExtension(system: ActorSystem) extends Extension {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db

  private val gcmPublisher = system.actorOf(GooglePushDelivery.props("https://gcm-http.googleapis.com/gcm/send"), "gcm-delivery")
  private val firebasePublisher = system.actorOf(GooglePushDelivery.props("https://fcm.googleapis.com/gcm/send"), "fcm-delivery")

  private val gcmStream = new DeliveryStream(gcmPublisher)(system).stream
  private val firebaseStream = new DeliveryStream(firebasePublisher)(system).stream

  private val config = GooglePushManagerConfig.load(system.settings.config.getConfig("services.google.push")).get

  private val keys: Map[Long, String] =
    (config.keys map {
      case GooglePushKey(projectId, key) ⇒ projectId → key
    }).toMap

  def send(projectId: Long, message: GooglePushMessage): Unit =
    keys get projectId match {
      case Some(key) ⇒
        gcmPublisher ! GooglePushDelivery.Delivery(message, key)
      case None ⇒
        log.warning("Key not found for projectId: {}", projectId)
    }

  def fetchCreds(authIds: Set[Long]): Future[Seq[GCMPushCredentials]] =
    db.run(GooglePushCredentialsRepo.find(authIds))
}
