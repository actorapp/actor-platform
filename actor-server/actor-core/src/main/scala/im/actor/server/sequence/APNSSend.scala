package im.actor.server.sequence

import akka.actor.ActorSystem
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import com.relayrides.pushy.apns.util.{ SimpleApnsPushNotification, TokenUtil }
import im.actor.server.model.push.ApplePushCredentials
import scodec.bits.BitVector

import scala.collection.concurrent.TrieMap

trait APNSSend {

  private val listeners = TrieMap.empty[String, PushFutureListener]

  protected def sendNotification(payload: String, creds: ApplePushCredentials, userId: Int)(implicit client: ApplePushExtension#Client, system: ActorSystem) = {
    // when topic is null, it will be taken from APNs certificate
    // http://relayrides.github.io/pushy/apidocs/0.6/com/relayrides/pushy/apns/ApnsPushNotification.html#getTopic--
    val token = BitVector(creds.token.toByteArray).toHex
    val topic: String = creds.bundleId.map(_.value).orNull
    system.log.debug("Sending APNS, token: {}, key: {}, isVoip: {}", token, creds.apnsKey, creds.isVoip)
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
