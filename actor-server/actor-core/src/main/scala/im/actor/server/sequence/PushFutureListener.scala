package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.ByteString
import com.relayrides.pushy.apns.PushNotificationResponse
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import im.actor.util.log.AnyRefLogSource
import io.netty.util.concurrent.{ Future, GenericFutureListener }

import scala.util.{ Failure, Success, Try }

final class PushFutureListener(userId: Int, token: ByteString)(implicit system: ActorSystem)
  extends GenericFutureListener[Future[PushNotificationResponse[SimpleApnsPushNotification]]] with AnyRefLogSource {

  private val log = Logging(system, this)
  private val seqUpdExt = SeqUpdatesExtension(system)

  def operationComplete(future: Future[PushNotificationResponse[SimpleApnsPushNotification]]): Unit = {
    Try(future.get()) match {
      case Success(response) ⇒
        if (!response.isAccepted) {
          log.warning("APNS rejected notification with reason: {}", response.getRejectionReason)
          Option(response.getTokenInvalidationTimestamp) foreach { ts ⇒
            log.warning("APNS token for user: {} invalidated at {}. Deleting token now", userId, ts)
            seqUpdExt.deleteApplePushCredentials(token.toByteArray)
          }
        }
      case Failure(e) ⇒
        log.error(e, "Failed to send APNS notification")
    }
  }

}
