package im.actor.server.push.google

import akka.actor._
import akka.event.Logging
import im.actor.server.db.DbExtension
import im.actor.server.model.push.GCMPushCredentials
import im.actor.server.persist.push.GooglePushCredentialsRepo

import scala.concurrent.Future

object GCMPushExtension extends ExtensionId[GCMPushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): GCMPushExtension = new GCMPushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = GCMPushExtension
}

final class GCMPushExtension(system: ActorSystem) extends Extension {

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db

  private val config = GooglePushManagerConfig.loadGCM.get

  private val gcmPublisher = system.actorOf(GooglePushDelivery.props("https://gcm-http.googleapis.com/gcm/send"), "gcm-delivery")

  private val gcmStream = new DeliveryStream(gcmPublisher, "GCM", remove)(system).stream

  def send(projectId: Long, message: GooglePushMessage): Unit =
    config.keyMap get projectId match {
      case Some(key) ⇒
        gcmPublisher ! GooglePushDelivery.Delivery(message, key)
      case None ⇒
        log.warning("Key not found for projectId: {}", projectId)
    }

  private def remove(regId: String): Future[Int] =
    db.run(GooglePushCredentialsRepo.deleteByToken(regId))

  def fetchCreds(authIds: Set[Long]): Future[Seq[GCMPushCredentials]] =
    db.run(GooglePushCredentialsRepo.find(authIds))
}
