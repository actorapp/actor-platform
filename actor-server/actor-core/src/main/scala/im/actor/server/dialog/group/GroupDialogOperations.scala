package im.actor.server.dialog.group

import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.server.dialog.{ DialogCommands, DialogId, DialogIdContainer, GroupDialogId }
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.{ ExecutionContext, Future }

object GroupDialogOperations {
  import DialogCommands._

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    region:  GroupDialogRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] =
    (region.ref ? SendMessage(groupDialogId(groupId), senderUserId, senderAuthId, randomId, message, isFat)).mapTo[SeqStateDate]

  def messageReceived(groupId: Int, receiverUserId: Int, date: Long)(
    implicit
    timeout: Timeout,
    region:  GroupDialogRegion,
    ec:      ExecutionContext
  ): Future[Unit] =
    (region.ref ? MessageReceived(groupDialogId(groupId), receiverUserId, date)).mapTo[MessageReceivedAck] map (_ ⇒ ())

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long)(
    implicit
    timeout: Timeout,
    region:  GroupDialogRegion,
    ec:      ExecutionContext
  ): Future[Unit] =
    (region.ref ? MessageRead(groupDialogId(groupId), readerUserId, readerAuthId, date)).mapTo[MessageReadAck] map (_ ⇒ ())

  private def groupDialogId(gid: Int) = DialogIdContainer().withGroup(DialogId.group(gid))

}
