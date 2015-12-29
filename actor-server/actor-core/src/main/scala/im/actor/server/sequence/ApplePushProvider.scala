package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.relayrides.pushy.apns.PushManager
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.server.db.DbExtension
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.persist.HistoryMessageRepo

private[sequence] final class ApplePushProvider(userId: Int, applePushManager: ApplePushManager, system: ActorSystem) extends PushProvider {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val db = DbExtension(system).db

  def deliverInvisible(seq: Int, creds: ApplePushCredentials): Unit = {
    withMgr(creds.apnsKey) { mgr ⇒
      log.debug("Delivering invisible(seq:{}) to apnsKey: {}", seq, creds.apnsKey)
      db.run(HistoryMessageRepo.getUnreadTotal(userId)) foreach { unreadTotal ⇒
        val builder =
          new ApnsPayloadBuilder()
            .addCustomProperty("seq", seq)
            .setContentAvailable(true)
            .setSoundFileName("")

        builder.setBadgeNumber(unreadTotal)

        val payload = builder.buildWithDefaultMaximumLength()

        mgr.getQueue.add(new SimpleApnsPushNotification(creds.token.toByteArray, payload))
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
    withMgr(creds.apnsKey) { mgr ⇒
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
      mgr.getQueue.add(new SimpleApnsPushNotification(creds.token.toByteArray, payload))
    }
  }

  private def withMgr[A](key: Int)(f: PushManager[SimpleApnsPushNotification] ⇒ A): Unit = {
    applePushManager.getInstance(key) match {
      case Some(mgr) ⇒ f(mgr)
      case None ⇒
        log.warning("No apple push configured for apns-key: {}", key)
    }
  }
}