package im.actor.server.sequence

import akka.actor.ActorSystem
import com.relayrides.pushy.apns.util.{ SimpleApnsPushNotification, TokenUtil }
import im.actor.server.model.push.ApplePushCredentials

import scala.collection.concurrent.TrieMap

trait APNSSend {

  private val listeners = TrieMap.empty[Int, PushFutureListener]

  protected def sendNotification(payload: String, creds: ApplePushCredentials, userId: Int)(implicit client: ApplePushExtension#Client, system: ActorSystem) = {
    system.log.debug("Sending APNS, token: {}", creds.token.toStringUtf8)
    // when topic is null, it will be taken from APNs certificate
    // http://relayrides.github.io/pushy/apidocs/0.6/com/relayrides/pushy/apns/ApnsPushNotification.html#getTopic--
    val token = TokenUtil.sanitizeTokenString(creds.token.toStringUtf8)
    system.log.debug("Sending APNS, sanitized token: {}", token)
    val notification = new SimpleApnsPushNotification(token, null, payload)
    val listener = listeners.getOrElseUpdate(creds.apnsKey, new PushFutureListener(userId, creds.token)(system))
    client.sendNotification(notification).addListener(listener)
  }

}
