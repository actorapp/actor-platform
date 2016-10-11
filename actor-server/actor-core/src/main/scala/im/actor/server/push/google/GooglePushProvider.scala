package im.actor.server.push.google

import akka.actor.ActorSystem
import im.actor.server.model.push.{ FirebasePushCredentials, GCMPushCredentials, GooglePushCredentials }
import im.actor.server.push.PushProvider
import im.actor.server.sequence.PushData

final class GooglePushProvider(userId: Int, system: ActorSystem) extends PushProvider {
  private val gcmPushExt = GCMPushExtension(system)
  private val firebasePushExt = FirebasePushExtension(system)

  def deliverInvisible(seq: Int, creds: GooglePushCredentials): Unit = {
    val message = GooglePushMessage(
      to = creds.regId,
      collapse_key = Some(s"seq-invisible-${userId.toString}"),
      data = Some(
        Map(
          "seq" → seq.toString,
          "_authId" → creds.authId.toString
        )
      ),
      time_to_live = None
    )
    creds match {
      case _: GCMPushCredentials ⇒
        gcmPushExt.send(creds.projectId, message)
      case _: FirebasePushCredentials ⇒
        firebasePushExt.send(creds.projectId, message)
    }
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
      data = Some(
        Map(
          "seq" → seq.toString,
          "_authId" → creds.authId.toString
        ) ++ (data.text match {
            case text if text.nonEmpty && isTextEnabled ⇒
              Map("message" → text)
            case _ ⇒ Map.empty
          })
      ),
      time_to_live = None
    )
    creds match {
      case _: GCMPushCredentials ⇒
        gcmPushExt.send(creds.projectId, message)
      case _: FirebasePushCredentials ⇒
        firebasePushExt.send(creds.projectId, message)
    }
  }
}
