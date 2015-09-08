package im.actor.server.dialog.privat

import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.server.dialog.DialogId
import im.actor.server.dialog.PrivateDialogCommands._
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.{ ExecutionContext, Future }

object PrivateDialogErrors {

  final object MessageToSelf extends Exception("Private dialog with self is not allowed")

}

object PrivateDialogOperations {
  def sendMessage(toUser: Int, senderId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = {
    val id = DialogId.privat(senderId, toUser)
    (region.ref ? SendMessage(id, id.origin(senderId), senderAuthId, randomId, message, isFat)).mapTo[SeqStateDate]
  }

  def messageReceived(receiverUserId: Int, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val id = DialogId.privat(peerUserId, receiverUserId)
    (region.ref ? MessageReceived(id, id.origin(receiverUserId), date)).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(readerUserId: Int, readerAuthId: Long, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    val id = DialogId.privat(peerUserId, readerUserId)
    (region.ref ? MessageRead(id, id.origin(readerUserId), readerAuthId, date)).mapTo[MessageReadAck] map (_ ⇒ ())
  }
}
