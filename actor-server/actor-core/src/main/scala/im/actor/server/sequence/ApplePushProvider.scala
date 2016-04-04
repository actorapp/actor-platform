package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.relayrides.pushy.apns.ApnsClient
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification, TokenUtil }
import im.actor.server.db.DbExtension
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.persist.HistoryMessageRepo

import scala.collection.concurrent.TrieMap

private[sequence] final class ApplePushProvider(userId: Int, system: ActorSystem) extends PushProvider {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db
  private val applePushExt = ApplePushExtension(system)

  private val listeners = TrieMap.empty[Int, PushFutureListener]

  def deliverInvisible(seq: Int, creds: ApplePushCredentials): Unit = {
    withClient(creds.apnsKey) { implicit client ⇒
      log.debug("Delivering invisible(seq:{}) to apnsKey: {}", seq, creds.apnsKey)
      db.run(HistoryMessageRepo.getUnreadTotal(userId)) foreach { unreadTotal ⇒
        val payload =
          new ApnsPayloadBuilder()
            .addCustomProperty("seq", seq)
            .setContentAvailable(true)
            .setSoundFileName("")
            .setBadgeNumber(unreadTotal)
            .buildWithDefaultMaximumLength()

        sendNotification(payload, creds)
      }
    }
  }

  def deliverVisible(
    seq:                Int,
    creds:              ApplePushCredentials,
    data:               PushData,
    isTextEnabled:      Boolean,
    isSoundEnabled:     Boolean,
    isVibrationEnabled: Boolean
  ): Unit = {
    withClient(creds.apnsKey) { implicit client ⇒
      val builder =
        new ApnsPayloadBuilder()
          .addCustomProperty("seq", seq)
          .setContentAvailable(true)

      if (data.text.nonEmpty && isTextEnabled)
        builder.setAlertBody(data.text)
      else if (data.censoredText.nonEmpty)
        builder.setAlertBody(data.censoredText)

      if (isSoundEnabled)
        builder.setSoundFileName("iapetus.caf")

      val payload = builder.buildWithDefaultMaximumLength()

      sendNotification(payload, creds)
    }
  }

  private def sendNotification(payload: String, creds: ApplePushCredentials)(implicit client: ApplePushExtension#Client) = {
    // when topic is null, it will be taken from APNs certificate
    // http://relayrides.github.io/pushy/apidocs/0.6/com/relayrides/pushy/apns/ApnsPushNotification.html#getToken--
    val notification = new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(creds.token.toStringUtf8), null, payload)
    val listener = listeners.getOrElseUpdate(creds.apnsKey, new PushFutureListener(userId, creds.token)(system))
    client.sendNotification(notification).addListener(listener)
  }

  private def withClient[A](key: Int)(f: ApnsClient[SimpleApnsPushNotification] ⇒ A): Unit = {
    applePushExt.clientFuture(key) match {
      case Some(futureClient) ⇒ futureClient foreach { f(_) }
      case None ⇒
        log.warning("No apple push configured for apns-key: {}", key)
    }
  }
}