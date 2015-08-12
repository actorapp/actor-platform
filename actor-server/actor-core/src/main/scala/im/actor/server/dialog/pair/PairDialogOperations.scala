package im.actor.server.dialog.pair

import akka.util.Timeout
import im.actor.server.dialog.PairDialogCommands._
import im.actor.server.sequence.SeqStateDate
import akka.pattern.ask
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }

import scala.concurrent.{ Future, ExecutionContext }

object PairDialogOperations {
  def sendMessage(toUser: Int, fromUser: Int, fromUserAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    region:  PairDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = {
    val path = DialogPath(toUser, fromUser)
    (region.ref ? SendMessage(path.toString, fromUser, fromUserAuthId, randomId, message, isFat)).mapTo[SeqStateDate]
  }

  def messageReceived(receiverUserId: Int, peerUserId: Int, date: Long)(
    implicit
    region:  PairDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val path = DialogPath(receiverUserId, peerUserId)
    (region.ref ? MessageReceived(path.toString, receiverUserId, date)).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(readerUserId: Int, readerAuthId: Long, peerUserId: Int, date: Long)(
    implicit
    region:  PairDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val path = DialogPath(readerUserId, peerUserId)
    (region.ref ? MessageRead(path.toString, readerUserId, readerAuthId, date)).mapTo[MessageReadAck] map (_ ⇒ ())
  }

}
