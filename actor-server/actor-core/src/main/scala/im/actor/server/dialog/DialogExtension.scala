package im.actor.server.dialog

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.ApiPeer
import im.actor.api.rpc.peers.ApiPeerType._
import im.actor.extension.InternalExtensions
import im.actor.server.dialog.DialogCommands._
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.dialog.privat.PrivateDialogRegion
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension {
  DialogProcessor.register()

  val InternalDialogExtensions = "modules.messaging.extensions"

  val privateRegion: PrivateDialogRegion = PrivateDialogRegion.start()(system)
  val groupRegion: GroupDialogRegion = GroupDialogRegion.start()(system)

  implicit val s: ActorSystem = system
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds) // TODO: configurable

  def sendMessage(peer: ApiPeer, senderUserId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false): Future[SeqStateDate] = {
    (peer.`type` match {
      case Private ⇒
        privateRegion.ref ? SendMessage(privatDialogId(senderUserId, peer.id), senderUserId, senderAuthId, randomId, message, isFat)
      case Group ⇒
        groupRegion.ref ? SendMessage(groupDialogId(peer.id), senderUserId, senderAuthId, randomId, message, isFat)
    }).mapTo[SeqStateDate]
  }

  def messageReceived(peer: ApiPeer, receiverUserId: Int, date: Long): Future[Unit] = {
    (peer.`type` match {
      case Private ⇒ privateRegion.ref ? MessageReceived(privatDialogId(peer.id, receiverUserId), receiverUserId, date)
      case Group   ⇒ groupRegion.ref ? MessageReceived(groupDialogId(peer.id), receiverUserId, date)

    }).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(peer: ApiPeer, readerUserId: Int, readerAuthId: Long, date: Long): Future[Unit] = {
    (peer.`type` match {
      case Private ⇒ privateRegion.ref ? MessageRead(privatDialogId(peer.id, readerUserId), readerUserId, readerAuthId, date)
      case Group   ⇒ groupRegion.ref ? MessageRead(groupDialogId(peer.id), readerUserId, readerAuthId, date)
    }).mapTo[MessageReadAck] map (_ ⇒ ())
  }

  def getDeliveryExtension(extensions: Seq[ApiExtension]): DeliveryExtension = {
    extensions match {
      case Seq() ⇒
        system.log.debug("No delivery extensions, using default one")
        new ActorDelivery()
      case ext +: tail ⇒
        system.log.debug("Got extensions: {}", extensions)
        val idToName = InternalExtensions.extensions(InternalDialogExtensions)
        idToName.get(ext.id) flatMap { className ⇒
          val extension = InternalExtensions.extensionOf[DeliveryExtension](className, system, ext.data).toOption
          system.log.debug("Created delivery extension: {}", extension)
          extension
        } getOrElse {
          val err = s"Dialog extension with id: ${ext.id} was not found"
          system.log.error(err)
          throw new Exception(err)
        }
    }
  }

  private def groupDialogId(gid: Int) = DialogIdContainer().withGroup(DialogId.group(gid))

  private def privatDialogId(a: Int, b: Int) = DialogIdContainer().withPrivat(DialogId.privat(a, b))
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)
}
