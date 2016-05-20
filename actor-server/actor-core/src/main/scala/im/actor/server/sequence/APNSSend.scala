package im.actor.server.sequence

import akka.actor.ActorSystem
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
    // when topic is null, it will be taken from APNs certificate
    // http://relayrides.github.io/pushy/apidocs/0.6/com/relayrides/pushy/apns/ApnsPushNotification.html#getTopic--
    val token = BitVector(creds.token.toByteArray).toHex
    val topic: String = (creds.apnsKey, creds.bundleId) match {
      case (_, Some(bundleId)) ⇒ bundleId.value
      case (Some(key), _)      ⇒ ApplePushExtension(system).apnsBundleId.get(key.value).orNull
      case _ ⇒
        system.log.warning("Wrong creds format on sending notification. Creds: {}", creds)
        null
    }
    system.log.debug("Sending APNS, token: {}, key: {}, isVoip: {}, topic: {}", token, creds.apnsKey, creds.isVoip, topic)
    val notification = new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(token), topic, payload)
    val listener = listeners.getOrElseUpdate(token, new PushFutureListener(userId, creds, extractCredsId(creds))(system))
    client.sendNotification(notification).addListener(listener)
  }

  protected def extractCredsId(creds: ApplePushCredentials): String = (creds.apnsKey, creds.bundleId) match {
    case (Some(Int32Value(key)), _)       ⇒ key.toString
    case (_, Some(StringValue(bundleId))) ⇒ bundleId
    case _                                ⇒ throw new RuntimeException("Wrong credentials format")
  }

}
