package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import com.relayrides.pushy.apns.ApnsClient
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.server.dialog.DialogExtension
import im.actor.server.model.push.ApplePushCredentials

private[sequence] final class ApplePushProvider(userId: Int)(implicit system: ActorSystem) extends PushProvider with APNSSend {
  import system.dispatcher

  private val log = Logging(system, getClass)
  private val dialogExt = DialogExtension(system)
  private val applePushExt = ApplePushExtension(system)

  def deliverInvisible(seq: Int, creds: ApplePushCredentials): Unit = {
    withClient(creds) { implicit client ⇒
      if (isLegacyCreds(creds)) {
        log.debug("Delivering invisible(seq:{}) to apnsKey: {}", seq, creds.apnsKey)
        dialogExt.getUnreadTotal(userId) foreach { unreadTotal ⇒
          val payload =
            new ApnsPayloadBuilder()
              .addCustomProperty("seq", seq)
              .setContentAvailable(true)
              .setSoundFileName("")
              .setBadgeNumber(unreadTotal)
              .buildWithDefaultMaximumLength()

          sendNotification(payload, creds, userId)
        }
      } else {
        log.debug("Delivering invisible(seq:{}) to bundleId: {}", seq, creds.bundleId)
        sendNotification(payload = seqOnly(seq), creds, userId)
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
    withClient(creds) { implicit client ⇒
      if (isLegacyCreds(creds)) {
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
      } else {
        sendNotification(payload = seqOnly(seq), creds, userId)
      }
    }
  }

  private def seqOnly(seq: Int): String =
    new ApnsPayloadBuilder()
      .addCustomProperty("seq", seq)
      .buildWithDefaultMaximumLength()

  private def isLegacyCreds(creds: ApplePushCredentials) = creds.bundleId.isEmpty

  private def withClient[A](creds: ApplePushCredentials)(f: ApnsClient[SimpleApnsPushNotification] ⇒ A): Unit = {
    val credsKey = extractCredsId(creds)
    applePushExt.client(credsKey) match {
      case Some(futureClient) ⇒ futureClient foreach { f(_) }
      case None ⇒
        log.warning("No apple push configured for: {}", credsKey)
    }
  }

}