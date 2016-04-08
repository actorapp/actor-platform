package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.relayrides.pushy.apns.PushNotificationResponse
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification
import im.actor.server.model.push.ApplePushCredentials
import im.actor.util.log.AnyRefLogSource
import io.netty.util.concurrent.{Future, GenericFutureListener}
import scodec.bits.BitVector

import scala.util.{Failure, Success, Try}

final class PushFutureListener(userId: Int, creds: ApplePushCredentials)(implicit system: ActorSystem)
  extends GenericFutureListener[Future[PushNotificationResponse[SimpleApnsPushNotification]]] with AnyRefLogSource {

  private val log = Logging(system, this)
  private val seqUpdExt = SeqUpdatesExtension(system)
  private val tokenBytes = creds.token.toByteArray
  private val tokenString = BitVector(tokenBytes).toHex

  def operationComplete(future: Future[PushNotificationResponse[SimpleApnsPushNotification]]): Unit = {
    Try(future.get()) match {
      case Success(response) ⇒
        log.debug("APNS send complete, user: {}, token: {}, apnsKey: {}, isVoip: {}",
          userId, tokenString, creds.apnsKey, creds.isVoip)
        if (response.isAccepted) {
          log.debug("Successfully delivered APNS notification to user: {}, token: {}, apnsKey: {}, isVoip: {}",
            userId, tokenString, creds.apnsKey, creds.isVoip)
        } else {
          log.warning(s"APNS rejected notification for user: {}, token: {}, apnsKey: {}, isVoip: {}, with reason: ${response.getRejectionReason}",
            userId, tokenString, creds.apnsKey, creds.isVoip)
          Option(response.getTokenInvalidationTimestamp) foreach { ts ⇒
            log.warning("APNS token: {} for user: {} invalidated at {}. Deleting token now", tokenString, userId, ts)
            seqUpdExt.deleteApplePushCredentials(tokenBytes)
          }
        }
      case Failure(e) ⇒
        log.error(e, "Failed to send APNS notification for user: {}, token: {}, apnsKey: {}, isVoip: {}",
          userId, tokenString, creds.apnsKey, creds.isVoip)
    }
  }

}
