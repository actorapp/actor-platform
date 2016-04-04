package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.relayrides.pushy.apns.ApnsClient
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.server.db.DbExtension
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.persist.HistoryMessageRepo

private[sequence] final class ApplePushProvider(userId: Int)(implicit system: ActorSystem) extends PushProvider with APNSSend {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db
  private val applePushExt = ApplePushExtension(system)

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

        sendNotification(payload, creds, userId)
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

      sendNotification(payload, creds, userId)
    }
  }

  private def withClient[A](key: Int)(f: ApnsClient[SimpleApnsPushNotification] ⇒ A): Unit = {
    applePushExt.clientFuture(key) match {
      case Some(futureClient) ⇒ futureClient foreach { f(_) }
      case None ⇒
        log.warning("No apple push configured for apns-key: {}", key)
    }
  }
}