package im.actor.server.push

import akka.actor.ActorSystem
import com.google.android.gcm.server.{ Message, Sender }
import slick.driver.PostgresDriver.api._

import scala.concurrent._

import im.actor.server.models
import im.actor.server.persist

// FIXME: #perf pinned dispatcher
private[push] class GooglePusher(gcmSender: Sender, db: Database)(implicit system: ActorSystem) extends VendorPush {
  implicit val ec: ExecutionContext = system.dispatcher

  def deliverGooglePush(creds: models.push.GooglePushCredentials, authId: Long, seq: Int): Unit = {
    system.log.debug("Delivering google push, authId: {}, seq: {}", authId, seq)

    val builder = (new Message.Builder)
      .collapseKey(authId.toString)
      .addData("seq", seq.toString)

    val message = builder.build()

    val resultFuture = Future { blocking { gcmSender.send(message, creds.regId, 3) } }

    resultFuture.map { result ⇒
      system.log.debug("Google push result messageId: {}, error: {}", result.getMessageId, result.getErrorCodeName)
    }.onFailure {
      case e ⇒ system.log.error(e, "Failed to deliver google push")
    }
  }
}