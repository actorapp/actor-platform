package im.actor.server.sequence

import akka.actor.ActorSystem
import akka.event.Logging
import com.google.android.gcm.server.{ Sender, Message }
import im.actor.server.model.push.GooglePushCredentials

private[sequence] final class GooglePushProvider(userId: Int, googlePushManager: GooglePushManager, system: ActorSystem) extends PushProvider {
  private val Retries = 3

  private val log = Logging(system, getClass)

  def deliverInvisible(seq: Int, creds: GooglePushCredentials): Unit = {
    withMgr(creds.projectId) { mgr ⇒
      val message =
        new Message.Builder()
          .collapseKey(s"seq-invisible-${userId.toString}")
          .addData("seq", seq.toString)
          .build()

      mgr.send(message, creds.regId, Retries)
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
    withMgr(creds.projectId) { mgr ⇒
      val builder = new Message.Builder()
        .collapseKey(s"seq-visible-${userId.toString}")
        .addData("seq", seq.toString)

      val message =
        data.text match {
          case text if text.nonEmpty && isTextEnabled ⇒
            builder
              .addData("message", text)
              .build()
          case _ ⇒ builder.build()
        }

      mgr.send(message, creds.regId, Retries)
    }
  }

  private def withMgr[A](projectId: Long)(f: Sender ⇒ A) =
    googlePushManager.getInstance(projectId) match {
      case Some(mgr) ⇒ f(mgr)
      case None      ⇒ log.warning("No google push configured for project-id: {}", projectId)
    }
}