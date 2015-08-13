package im.actor.server.dialog.privat

import akka.util.Timeout
import im.actor.server.dialog.PrivateDialogCommands.Origin.{ RIGHT, LEFT }
import im.actor.server.dialog.PrivateDialogCommands._
import im.actor.server.sequence.SeqStateDate
import akka.pattern.ask
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }

import scala.concurrent.{ Future, ExecutionContext }

object PrivateDialogOperations {
  def sendMessage(toUser: Int, senderId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = {
    val r = Routing(senderId, toUser)
    (region.ref ? SendMessage(r.left, r.right, r.origin(senderId), senderAuthId, randomId, message, isFat)).mapTo[SeqStateDate]
  }

  def messageReceived(receiverUserId: Int, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val r = Routing(peerUserId, receiverUserId)
    (region.ref ? MessageReceived(r.left, r.right, r.origin(receiverUserId), date)).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(readerUserId: Int, readerAuthId: Long, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val r = Routing(peerUserId, readerUserId)
    (region.ref ? MessageRead(r.left, r.right, r.origin(readerUserId), readerAuthId, date)).mapTo[MessageReadAck] map (_ ⇒ ())
  }

  case class Routing(private val a: Int, private val b: Int) {
    require(a != b, "Private dialog with self is not allowed")
    val (left, right) = if (a > b) (b, a) else (a, b)
    def origin(uid: Int): Origin = if (uid == left) LEFT else RIGHT
  }

}
