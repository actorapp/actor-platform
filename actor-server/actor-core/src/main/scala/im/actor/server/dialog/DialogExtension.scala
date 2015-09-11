package im.actor.server.dialog

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.api.rpc.peers.ApiPeerType._
import im.actor.api.rpc.peers.ApiPeerType.ApiPeerType
import im.actor.server.dialog.DialogCommands._
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.dialog.privat.PrivateDialogRegion
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension {
  DialogProcessor.register()

  val privateRegion: PrivateDialogRegion = PrivateDialogRegion.start()(system)
  val groupRegion: GroupDialogRegion = GroupDialogRegion.start()(system)

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  def sendMessage(peerType: ApiPeerType, peerId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false): Future[SeqStateDate] = {
    (peerType match {
      case Private ⇒
        privateRegion.ref ? SendMessage(privatDialogId(senderUserId, peerId), senderUserId, senderAuthId, randomId, message, isFat)
      case Group ⇒
        groupRegion.ref ? SendMessage(groupDialogId(peerId), senderUserId, senderAuthId, randomId, message, isFat)
    }).mapTo[SeqStateDate]
  }

  def messageReceived(peerType: ApiPeerType, peerId: Int, receiverUserId: Int, date: Long): Future[Unit] = {
    (peerType match {
      case Private ⇒ privateRegion.ref ? MessageReceived(privatDialogId(peerId, receiverUserId), receiverUserId, date)
      case Group   ⇒ groupRegion.ref ? MessageReceived(groupDialogId(peerId), receiverUserId, date)

    }).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(peerType: ApiPeerType, peerId: Int, readerUserId: Int, readerAuthId: Long, date: Long): Future[Unit] = {
    (peerType match {
      case Private ⇒ privateRegion.ref ? MessageRead(privatDialogId(peerId, readerUserId), readerUserId, readerAuthId, date)
      case Group   ⇒ groupRegion.ref ? MessageRead(groupDialogId(peerId), readerUserId, readerAuthId, date)
    }).mapTo[MessageReadAck] map (_ ⇒ ())
  }

  private def groupDialogId(gid: Int) = DialogIdContainer().withGroup(DialogId.group(gid))

  private def privatDialogId(a: Int, b: Int) = DialogIdContainer().withPrivat(DialogId.privat(a, b))
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)
}