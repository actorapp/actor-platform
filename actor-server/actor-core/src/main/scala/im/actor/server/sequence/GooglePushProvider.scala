package im.actor.server.sequence

import akka.actor.ActorSystem
import im.actor.server.model.push.GooglePushCredentials

private[sequence] final class GooglePushProvider(userId: Int, system: ActorSystem) extends PushProvider {
  private val googlePushExt = GooglePushExtension(system)

  def deliverInvisible(seq: Int, creds: GooglePushCredentials): Unit = {
    val message = GooglePushMessage(
      to = creds.regId,
      collapse_key = Some(s"seq-invisible-${userId.toString}"),
      data = Some(Map("seq" → seq.toString)),
      time_to_live = None
    )

    googlePushExt.send(creds.projectId, message)
  }

  def deliverVisible(
    seq:                Int,
    creds:              GooglePushCredentials,
    data:               PushData,
    isTextEnabled:      Boolean,
    isSoundEnabled:     Boolean,
    isVibrationEnabled: Boolean
  ): Unit = {
    val message = GooglePushMessage(
      to = creds.regId,
      collapse_key = Some(s"seq-visible-${userId.toString}"),
      data = Some(Map("seq" → seq.toString) ++ (
        data.text match {
          case text if text.nonEmpty && isTextEnabled ⇒
            Map("message" → text)
          case _ ⇒ Map.empty
        }
      )),
      time_to_live = None
    )

    googlePushExt.send(creds.projectId, message)
  }
}