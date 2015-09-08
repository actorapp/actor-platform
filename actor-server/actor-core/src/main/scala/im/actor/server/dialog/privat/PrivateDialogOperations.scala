package im.actor.server.dialog.privat

import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.server.dialog.{ DialogIdContainer, DialogId }
import im.actor.server.dialog.DialogCommands._
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
    (region.ref ? SendMessage(privatDialogId(senderId, toUser), senderId, senderAuthId, randomId, message, isFat)).mapTo[SeqStateDate]
  }

  def messageReceived(receiverUserId: Int, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    (region.ref ? MessageReceived(privatDialogId(peerUserId, receiverUserId), receiverUserId, date)).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(readerUserId: Int, readerAuthId: Long, peerUserId: Int, date: Long)(
    implicit
    region:  PrivateDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = {
    (region.ref ? MessageRead(privatDialogId(peerUserId, readerUserId), readerUserId, readerAuthId, date)).mapTo[MessageReadAck] map (_ ⇒ ())
  }

  private def privatDialogId(a: Int, b: Int) = DialogIdContainer().withPrivat(DialogId.privat(a, b))
}
