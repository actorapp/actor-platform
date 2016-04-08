package im.actor.server.sequence

import akka.actor.ActorSystem
import com.relayrides.pushy.apns.util.{ SimpleApnsPushNotification, TokenUtil }
import im.actor.server.model.push.ApplePushCredentials
import scodec.bits.BitVector

import scala.collection.concurrent.TrieMap

trait APNSSend {

  private val listeners = TrieMap.empty[Int, PushFutureListener]

  protected def sendNotification(payload: String, creds: ApplePushCredentials, userId: Int)(implicit client: ApplePushExtension#Client, system: ActorSystem) = {
    // when topic is null, it will be taken from APNs certificate
    // http://relayrides.github.io/pushy/apidocs/0.6/com/relayrides/pushy/apns/ApnsPushNotification.html#getTopic--
    val token = BitVector(creds.token.toByteArray).toHex
    system.log.debug("Sending APNS, token: {}", token)
    val notification = new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(token), null, payload)
    val listener = listeners.getOrElseUpdate(creds.apnsKey, new PushFutureListener(userId, creds.token)(system))
    client.sendNotification(notification).addListener(listener)
  }

}
