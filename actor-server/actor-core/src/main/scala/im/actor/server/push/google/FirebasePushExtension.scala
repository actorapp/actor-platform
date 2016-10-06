package im.actor.server.push.google

import akka.actor._
import akka.event.Logging
import im.actor.server.model.push.FirebasePushCredentials
import im.actor.server.persist.push.FirebasePushCredentialsKV

import scala.concurrent.Future

object FirebasePushExtension extends ExtensionId[FirebasePushExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FirebasePushExtension = new FirebasePushExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = FirebasePushExtension
}

final class FirebasePushExtension(system: ActorSystem) extends Extension {

  private val log = Logging(system, getClass)

  private val firebaseKV = new FirebasePushCredentialsKV()(system)

  private val config = GooglePushManagerConfig.loadFirebase.get

  private val firebasePublisher = system.actorOf(GooglePushDelivery.props("https://fcm.googleapis.com/fcm/send"), "fcm-delivery")

  private val firebaseStream = new DeliveryStream(firebasePublisher, "Firebase", remove)(system).stream

  def send(projectId: Long, message: GooglePushMessage): Unit =
    config.keyMap get projectId match {
      case Some(key) ⇒
        firebasePublisher ! GooglePushDelivery.Delivery(message, key)
      case None ⇒
        log.warning("Key not found for projectId: {}", projectId)
    }

  private def remove(regId: String): Future[Unit] =
    firebaseKV.deleteByToken(regId)

  def fetchCreds(authIds: Set[Long]): Future[Seq[FirebasePushCredentials]] =
    firebaseKV.find(authIds)

}
