package im.actor.server.dialog

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort, ApiMessage }
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.api.rpc.peers.ApiPeerType._
import im.actor.concurrent.FutureExt
import im.actor.extension.InternalExtensions
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogCommands._
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.dialog.privat.PrivateDialogRegion
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.models.{ Peer, PeerType, Dialog }
import im.actor.server.persist.{ HistoryMessage, DialogRepo }
import im.actor.server.sequence.SeqStateDate
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension {
  DialogProcessor.register()

  val InternalDialogExtensions = "modules.messaging.extensions"

  val privateRegion: PrivateDialogRegion = PrivateDialogRegion.start()(system)
  val groupRegion: GroupDialogRegion = GroupDialogRegion.start()(system)
  private val db = DbExtension(system).db
  private lazy val groupExt = GroupExtension(system)

  private implicit val s: ActorSystem = system
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds) // TODO: configurable

  def sendMessage(peer: ApiPeer, senderUserId: Int, senderAuthId: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false): Future[SeqStateDate] = {
    (peer.`type` match {
      case Private ⇒
        privateRegion.ref ? SendMessage(privatDialogId(senderUserId, peer.id), senderUserId, senderAuthId, randomId, message, isFat)
      case Group ⇒
        groupRegion.ref ? SendMessage(groupDialogId(peer.id), senderUserId, senderAuthId, randomId, message, isFat)
    }).mapTo[SeqStateDate]
  }

  def writeMessage(
    peer:         ApiPeer,
    senderUserId: Int,
    date:         DateTime,
    randomId:     Long,
    message:      ApiMessage
  ): Future[Unit] =
    (peer.`type` match {
      case Private ⇒
        privateRegion.ref ? WriteMessage(privatDialogId(senderUserId, peer.id), senderUserId, date.getMillis, randomId, message)
      case Group ⇒
        groupRegion.ref ? WriteMessage(groupDialogId(peer.id), senderUserId, date.getMillis, randomId, message)
    }) map (_ ⇒ ())

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

  def createGroupDialog(groupId: Int, userId: Int): Future[Unit] =
    (groupRegion.ref ? CreateForUser(groupDialogId(groupId), userId)) map (_ ⇒ ())

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

  def getUnreadCount(clientUserId: Int, historyOwner: Int, peer: Peer, ownerLastReadAt: DateTime): DBIO[Int] = {
    if (isSharedUser(historyOwner)) {
      for {
        isMember ← DBIO.from(groupExt.getMemberIds(peer.id) map { case (memberIds, _, _) ⇒ memberIds contains clientUserId })
        result ← if (isMember) HistoryMessage.getUnreadCount(historyOwner, peer, ownerLastReadAt) else DBIO.successful(0)
      } yield result
    } else {
      HistoryMessage.getUnreadCount(historyOwner, peer, ownerLastReadAt)
    }
  }

  def isSharedUser(userId: Int): Boolean = userId == 0

  def getGroupedDialogs(userId: Int) = {
    db.run(DialogRepo.findNotArchivedSortByCreatedAt(userId, None, Int.MaxValue)) flatMap { dialogModels ⇒
      val (groupModels, privateModels) = dialogModels.foldLeft((Vector.empty[Dialog], Vector.empty[Dialog])) {
        case ((groupModels, privateModels), dialog) ⇒
          if (dialog.peer.typ == PeerType.Group)
            (groupModels :+ dialog, privateModels)
          else
            (groupModels, privateModels :+ dialog)
      }

      for {
        groupDialogs ← db.run(DBIO.sequence(groupModels map getDialogShort))
        privateDialogs ← FutureExt.ftraverse(privateModels)(d ⇒ db.run(getDialogShort(d)))
      } yield {
        Vector(
          ApiDialogGroup("Groups", "groups", groupDialogs),
          ApiDialogGroup("Private", "privates", privateDialogs.toVector)
        )
      }
    }
  }

  private def getDialogShort(dialogModel: Dialog)(implicit ec: ExecutionContext): DBIO[ApiDialogShort] = {
    HistoryUtils.withHistoryOwner(dialogModel.peer, dialogModel.userId) { historyOwner ⇒
      for {
        messageOpt ← HistoryMessage.findNewest(historyOwner, dialogModel.peer) map (_.map(_.ofUser(dialogModel.userId)))
        unreadCount ← getUnreadCount(dialogModel.userId, historyOwner, dialogModel.peer, dialogModel.ownerLastReadAt)
      } yield ApiDialogShort(
        peer = ApiPeer(ApiPeerType(dialogModel.peer.typ.toInt), dialogModel.peer.id),
        counter = unreadCount,
        date = messageOpt.map(_.date.getMillis).getOrElse(0)
      )
    }
  }

  private def groupDialogId(gid: Int) = DialogIdContainer().withGroup(DialogId.group(gid))

  private def privatDialogId(a: Int, b: Int) = DialogIdContainer().withPrivat(DialogId.privat(a, b))
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)
}
