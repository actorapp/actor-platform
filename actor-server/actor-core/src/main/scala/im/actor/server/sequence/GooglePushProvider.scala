package im.actor.server.sequence

import akka.actor.ActorSystem
import im.actor.server.model.push.GooglePushCredentials

private[sequence] final class GooglePushProvider(userId: Int, googlePushManager: GooglePushManager, system: ActorSystem) extends PushProvider {
  def deliverInvisible(seq: Int, creds: GooglePushCredentials): Unit = {
    val message = GooglePushMessage(
      collapseKey = Some(s"seq-invisible-${userId.toString}"),
      data = Some(Map("seq" → seq.toString))
    )

    googlePushManager.send(creds.projectId, creds.regId, message)
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
      collapseKey = Some(s"seq-visible-${userId.toString}"),
      data = Some(Map("seq" → seq.toString) ++ (
        data.text match {
          case text if text.nonEmpty && isTextEnabled ⇒
            Map("message" → text)
          case _ ⇒ Map.empty
        }
      ))
    )

    googlePushManager.send(creds.projectId, creds.regId, message)
  }
}