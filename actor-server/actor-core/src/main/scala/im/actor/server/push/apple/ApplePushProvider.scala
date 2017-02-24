package im.actor.server.push.apple

import akka.actor.ActorSystem
import akka.event.Logging
import com.relayrides.pushy.apns.ApnsClient
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.server.dialog.DialogExtension
import im.actor.server.model.push.ApplePushCredentials
import im.actor.server.push.PushProvider
import im.actor.server.sequence.PushData

import scala.concurrent.Future

final class ApplePushProvider(userId: Int)(implicit system: ActorSystem) extends PushProvider with APNSSend {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val dialogExt = DialogExtension(system)
  private val applePushExt = ApplePushExtension(system)

  def deliverInvisible(seq: Int, creds: ApplePushCredentials): Unit = {
    withClient(creds) { implicit client ⇒
      if (isLegacyCreds(creds)) {
        log.debug("Delivering invisible(seq:{}) to apnsKey: {}", seq, creds.apnsKey)
        // according to https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/TheNotificationPayload.html#//apple_ref/doc/uid/TP40008194-CH107-SW6
        // silent notification should not contain `alert`, `sound`, or `badge` payload.
        val payload = new ApnsPayloadBuilder()
          .addCustomProperty("seq", seq)
          .setContentAvailable(true)
          .buildWithDefaultMaximumLength()
        sendNotification(payload, creds, userId)
      } else {
        log.debug("Delivering invisible(seq:{}) to bundleId: {}", seq, creds.bundleId)
        sendNotification(payload = seqOnly(seq), creds, userId)
      }
      deliverVoip(seq, creds.authId)
    }
  }

  def deliverVisible(
    seq:                Int,
    creds:              ApplePushCredentials,
    data:               PushData,
    isTextEnabled:      Boolean,
    isSoundEnabled:     Boolean,
    customSound:        Option[String],
    isVibrationEnabled: Boolean
  ): Unit = {
    withClient(creds) { implicit client ⇒
      if (isLegacyCreds(creds)) {
        dialogExt.getUnreadTotal(userId) foreach { total ⇒
          val builder =
            new ApnsPayloadBuilder()
              .addCustomProperty("seq", seq)
              .setContentAvailable(true)
              .setBadgeNumber(total)

          if (data.text.nonEmpty && isTextEnabled)
            builder.setAlertBody(data.text)
          else if (data.censoredText.nonEmpty)
            builder.setAlertBody(data.censoredText)

          if (isSoundEnabled)
            builder.setSoundFileName(customSound getOrElse "iapetus.caf")

          val payload = builder.buildWithDefaultMaximumLength()
          sendNotification(payload, creds, userId)
        }
      } else {
        sendNotification(payload = seqOnly(seq), creds, userId)
      }
      deliverVoip(seq, creds.authId)
    }
  }

  def deliverVoip(
    seq:    Int,
    authId: Long
  ): Unit = {
    applePushExt.fetchVoipCreds(Seq(authId).toSet).map(userId → _).map {
      case (userId, credsList) ⇒ {
        for {
          creds ← credsList
          credsId = extractCredsId(creds)
          clientFu ← applePushExt.voipClient(credsId)
          payload = new ApnsPayloadBuilder().buildWithDefaultMaximumLength()
          _ = clientFu foreach { implicit c ⇒ sendNotification(payload, creds, userId) }
        } yield ()
      }
    }

  }

  private def seqOnly(seq: Int): String =
    new ApnsPayloadBuilder()
      .addCustomProperty("seq", seq)
      .buildWithDefaultMaximumLength()

  private def isLegacyCreds(creds: ApplePushCredentials) = creds.bundleId.isEmpty

  private def withClient[A](creds: ApplePushCredentials)(f: ApnsClient ⇒ A): Unit = {
    val credsKey = extractCredsId(creds)
    applePushExt.client(credsKey) match {
      case Some(futureClient) ⇒ futureClient foreach { f(_) }
      case None ⇒
        log.warning("No apple push configured for: {}", credsKey)
    }
  }

}
