package im.actor.server.push.apple

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import com.relayrides.pushy.apns.PushNotificationResponse
import com.relayrides.pushy.apns.util.{ SimpleApnsPushNotification, TokenUtil }
import im.actor.server.model.push.ApplePushCredentials
import io.netty.util.concurrent.{ Future ⇒ NFuture }
import scodec.bits.BitVector

import scala.collection.concurrent.TrieMap

trait APNSSend {

  private val listeners = TrieMap.empty[String, PushFutureListener]

  protected def sendNotification(payload: String, creds: ApplePushCredentials, userId: Int)(implicit client: ApplePushExtension#Client, system: ActorSystem): NFuture[PushNotificationResponse[SimpleApnsPushNotification]] = {

    val log = Logging(system, getClass)
    val token = BitVector(creds.token.toByteArray).toHex

    log.debug(
      s"Searching topic for ApnsKey: {}, BundleId: {}, AuthId: {}, IsVoip: {}, token: $token",
      creds.apnsKey, creds.bundleId, creds.authId, creds.isVoip
    )

    val topic: String = (creds.apnsKey, creds.bundleId) match {
      case (_, Some(bundleId)) ⇒ bundleId.value
      case (Some(key), _)      ⇒ ApplePushExtension(system).apnsBundleId(creds.isVoip).get(key.value).orNull
      case _ ⇒
        log.warning("Wrong creds format on sending notification. Creds: {}", creds)
        null
    }

    val sanitizedToken = TokenUtil.sanitizeTokenString(token)
    log.debug(s"Sending APNS, token: {}, key: {}, isVoip: {}, topic: {}, payload: $payload", sanitizedToken, creds.apnsKey, creds.isVoip, topic)

    val notification = new SimpleApnsPushNotification(sanitizedToken, topic, payload)

    val listener = listeners.getOrElseUpdate(token, new PushFutureListener(userId, creds, extractCredsId(creds))(system))
    client.sendNotification(notification).addListener(listener)
  }

  protected def extractCredsId(creds: ApplePushCredentials): String = (creds.apnsKey, creds.bundleId) match {
    case (Some(Int32Value(key)), _)       ⇒ key.toString
    case (_, Some(StringValue(bundleId))) ⇒ bundleId
    case _                                ⇒ throw new RuntimeException("Wrong credentials format")
  }

}
