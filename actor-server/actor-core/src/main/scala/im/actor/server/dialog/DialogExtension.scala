package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.{ ApiDialogGroup, ApiDialogShort, ApiMessage }
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.extension.InternalExtensions
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogCommands._
import im.actor.server.group.GroupExtension
import im.actor.server.model._
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserExtension
import im.actor.types._
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

sealed trait DialogGroup {
  def key: String
  def title: String
}

object DialogGroups {
  object Favourites extends DialogGroup {
    override def key: String = "favourites"

    override def title: String = "Favourites"
  }

  object Privates extends DialogGroup {
    override def key: String = "privates"

    override def title: String = "Private"
  }

  object Groups extends DialogGroup {
    override def key: String = "groups"

    override def title: String = "Groups"
  }
}

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension with PeersImplicits {
  DialogProcessor.register()

  val InternalDialogExtensions = "modules.messaging.extensions"

  private val db = DbExtension(system).db
  private lazy val userExt = UserExtension(system)
  private lazy val groupExt = GroupExtension(system)

  private implicit val s: ActorSystem = system
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds) // TODO: configurable

  private val log = Logging(system, getClass)

  private def withValidPeer[A](peer: Peer, senderUserId: Int, failed: ⇒ Future[A] = Future.failed[A](DialogErrors.MessageToSelf))(f: ⇒ Future[A]): Future[A] =
    peer match {
      case Peer(PeerType.Private, id) if id == senderUserId ⇒
        log.error(s"Attempt to work with yourself, userId: $senderUserId")
        failed
      case _ ⇒ f
    }

  def sendMessage(
    peer:          ApiPeer,
    senderUserId:  UserId,
    senderAuthSid: Int,
    senderAuthId:  Option[Long], // required only in case of access hash check for private peer
    randomId:      RandomId,
    message:       ApiMessage,
    accessHash:    Option[Long]   = None,
    isFat:         Boolean        = false,
    forUserId:     Option[UserId] = None
  ): Future[SeqStateDate] =
    withValidPeer(peer.asModel, senderUserId, Future.successful(SeqStateDate())) {
      val sender = Peer.privat(senderUserId)
      // we don't set date here, cause actual date set inside dialog processor
      val sendMessage = SendMessage(
        origin = sender,
        dest = peer.asModel,
        senderAuthSid = senderAuthSid,
        senderAuthId = senderAuthId,
        date = None,
        randomId = randomId,
        message = message,
        accessHash = accessHash,
        isFat = isFat,
        forUserId = forUserId
      )
      (userExt.processorRegion.ref ? Envelope(sender).withSendMessage(sendMessage)).mapTo[SeqStateDate]
    }

  def ackSendMessage(peer: Peer, sm: SendMessage): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withSendMessage(sm)).mapTo[SendMessageAck] map (_ ⇒ ())

  def writeMessage(
    peer:         ApiPeer,
    senderUserId: Int,
    date:         Instant,
    randomId:     Long,
    message:      ApiMessage
  ): Future[Unit] =
    withValidPeer(peer.asModel, senderUserId, Future.successful(())) {
      val sender = Peer.privat(senderUserId)
      val writeMessage = WriteMessage(sender, peer.asModel, date.toEpochMilli, randomId, message)
      (userExt.processorRegion.ref ? Envelope(sender).withWriteMessage(writeMessage)).mapTo[WriteMessageAck] map (_ ⇒ ())
    }

  def writeMessageSelf(
    userId:       Int,
    peer:         ApiPeer,
    senderUserId: Int,
    date:         DateTime,
    randomId:     Long,
    message:      ApiMessage
  ): Future[Unit] =
    withValidPeer(Peer.privat(userId), peer.id, Future.successful(())) {
      (userExt.processorRegion.ref ?
        Envelope(Peer.privat(userId)).withWriteMessageSelf(WriteMessageSelf(
          dest = peer.asModel,
          senderUserId,
          date.getMillis,
          randomId,
          message
        ))) map (_ ⇒ ())
    }

  def messageReceived(peer: ApiPeer, receiverUserId: Int, date: Long): Future[Unit] =
    withValidPeer(peer.asModel, receiverUserId, Future.successful(())) {
      val now = Instant.now().toEpochMilli
      val receiver = Peer.privat(receiverUserId)
      val messageReceived = MessageReceived(receiver, peer.asModel, date, now)
      (userExt.processorRegion.ref ? Envelope(receiver).withMessageReceived(messageReceived)).mapTo[MessageReceivedAck] map (_ ⇒ ())
    }

  def ackMessageReceived(peer: Peer, mr: MessageReceived): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withMessageReceived(mr)).mapTo[MessageReceivedAck] map (_ ⇒ ())

  def messageRead(peer: ApiPeer, readerUserId: Int, readerAuthSid: Int, date: Long): Future[Unit] =
    withValidPeer(peer.asModel, readerUserId, Future.successful(())) {
      val now = Instant.now().toEpochMilli
      val reader = Peer.privat(readerUserId)
      val messageRead = MessageRead(reader, peer.asModel, readerAuthSid, date, now)
      (userExt.processorRegion.ref ? Envelope(reader).withMessageRead(messageRead)).mapTo[MessageReadAck] map (_ ⇒ ())
    }

  def ackMessageRead(peer: Peer, mr: MessageRead): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withMessageRead(mr)).mapTo[MessageReadAck] map (_ ⇒ ())

  def show(userId: Int, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withShow(Show(peer))).mapTo[SeqState]
    }

  def archive(userId: Int, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withArchive(Archive(peer))).mapTo[SeqState]
    }

  def favourite(userId: Int, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withFavourite(Favourite(peer))).mapTo[SeqState]
    }

  def unfavourite(userId: Int, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withUnfavourite(Unfavourite(peer))).mapTo[SeqState]
    }

  def delete(userId: Int, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withDelete(Delete(peer))).mapTo[SeqState]
    }

  def setReaction(userId: Int, authSid: Int, peer: Peer, randomId: Long, code: String): Future[SetReactionAck] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withSetReaction(SetReaction(
        origin = Peer.privat(userId),
        dest = peer,
        clientAuthSid = authSid,
        randomId = randomId,
        code = code
      ))).mapTo[SetReactionAck]
    }

  def removeReaction(userId: Int, authSid: Int, peer: Peer, randomId: Long, code: String): Future[RemoveReactionAck] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ? Envelope(Peer.privat(userId)).withRemoveReaction(RemoveReaction(
        origin = Peer.privat(userId),
        dest = peer,
        clientAuthSid = authSid,
        randomId = randomId,
        code = code
      ))).mapTo[RemoveReactionAck]
    }

  def ackSetReaction(peer: Peer, sr: SetReaction): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withSetReaction(sr)) map (_ ⇒ ())

  def ackRemoveReaction(peer: Peer, rr: RemoveReaction): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withRemoveReaction(rr)) map (_ ⇒ ())

  def updateCounters(peer: Peer, userId: Int): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withUpdateCounters(UpdateCounters(
      origin = Peer.privat(userId),
      dest = peer
    ))) map (_ ⇒ ())

  def ackUpdateCounters(peer: Peer, uc: UpdateCounters): Future[Unit] =
    (processorRegion(peer) ? Envelope(peer).withUpdateCounters(uc)) map (_ ⇒ ())

  def getDeliveryExtension(extensions: Seq[ApiExtension]): DeliveryExtension = {
    extensions match {
      case Seq() ⇒
        log.debug("No delivery extensions, using default one")
        new ActorDelivery()
      case ext +: tail ⇒
        log.debug("Got extensions: {}", extensions)
        val idToName = InternalExtensions.extensions(InternalDialogExtensions)
        idToName.get(ext.id) flatMap { className ⇒
          val extension = InternalExtensions.extensionOf[DeliveryExtension](className, system, ext.data).toOption
          log.debug("Created delivery extension: {}", extension)
          extension
        } getOrElse {
          val err = s"Dialog extension with id: ${ext.id} was not found"
          log.error(err)
          throw new Exception(err)
        }
    }
  }

  def getUnreadTotal(userId: Int): DBIO[Int] = HistoryMessageRepo.getUnreadTotal(userId)

  def getUnreadCount(clientUserId: Int, historyOwner: Int, peer: Peer, ownerLastReadAt: DateTime): DBIO[Int] = {
    if (isSharedUser(historyOwner)) {
      for {
        isMember ← DBIO.from(groupExt.getMemberIds(peer.id) map { case (memberIds, _, _) ⇒ memberIds contains clientUserId })
        result ← if (isMember) HistoryMessageRepo.getUnreadCount(historyOwner, clientUserId, peer, ownerLastReadAt) else DBIO.successful(0)
      } yield result
    } else {
      HistoryMessageRepo.getUnreadCount(historyOwner, clientUserId, peer, ownerLastReadAt)
    }
  }

  def isSharedUser(userId: Int): Boolean = userId == 0

  def fetchGroupedDialogs(userId: Int): Future[Map[DialogGroup, Vector[Dialog]]] =
    db.run {
      DialogRepo
        .fetchSortByLastMessageData(userId, None, Int.MaxValue)
        .map(_ filterNot (dialogWithSelf(userId, _)))
        .map { dialogs ⇒
          val (groupModels, privateModels, favouriteModels) =
            dialogs.foldLeft((Vector.empty[Dialog], Vector.empty[Dialog], Vector.empty[Dialog])) {
              case ((groupModels, privateModels, favouriteModels), dialog) ⇒
                if (dialog.isFavourite)
                  (groupModels, privateModels, favouriteModels :+ dialog)
                else if (dialog.peer.typ == PeerType.Group)
                  (groupModels :+ dialog, privateModels, favouriteModels)
                else if (dialog.peer.typ == PeerType.Private)
                  (groupModels, privateModels :+ dialog, favouriteModels)
                else throw new RuntimeException("Unknown dialog type")
            }

          Map(
            DialogGroups.Favourites → favouriteModels,
            DialogGroups.Privates → privateModels,
            DialogGroups.Groups → groupModels
          )
        }
    }

  def fetchGroupedDialogShorts(userId: Int): Future[Vector[ApiDialogGroup]] = {
    fetchGroupedDialogs(userId) flatMap { dialogsMap ⇒
      db.run {
        val groupModels = dialogsMap.getOrElse(DialogGroups.Groups, Vector.empty)
        val privateModels = dialogsMap.getOrElse(DialogGroups.Privates, Vector.empty)
        val favouriteModels = dialogsMap.getOrElse(DialogGroups.Favourites, Vector.empty)

        for {
          groupDialogs ← DBIO.sequence(groupModels map getDialogShortDBIO)
          privateDialogs ← DBIO.sequence(privateModels map getDialogShortDBIO)
          favouriteDialogs ← DBIO.sequence(favouriteModels map getDialogShortDBIO)
        } yield {
          val default = Vector(
            ApiDialogGroup(DialogGroups.Groups.title, DialogGroups.Groups.key, groupDialogs),
            ApiDialogGroup(DialogGroups.Privates.title, DialogGroups.Privates.key, privateDialogs)
          )

          if (favouriteDialogs.nonEmpty)
            ApiDialogGroup(DialogGroups.Favourites.title, DialogGroups.Favourites.key, favouriteDialogs) +: default
          else default
        }
      }
    }
  }

  def dialogWithSelf(userId: Int, dialog: Dialog): Boolean =
    dialog.peer.typ == PeerType.Private && dialog.peer.id == userId

  def fetchReactions(peer: Peer, clientUserId: Int, randomId: Long): DBIO[Seq[MessageReaction]] =
    ReactionEventRepo.fetch(DialogId(peer, clientUserId), randomId) map reactions

  def fetchReactions(peer: Peer, clientUserId: Int, randomIds: Set[Long]): DBIO[Map[Long, Seq[MessageReaction]]] =
    for {
      events ← ReactionEventRepo.fetch(DialogId(peer, clientUserId), randomIds)
    } yield events.view.groupBy(_.randomId).mapValues(reactions)

  private def reactions(events: Seq[ReactionEvent]): Seq[MessageReaction] = {
    (events.view groupBy (_.code) mapValues (_ map (_.userId)) map {
      case (code, userIds) ⇒ MessageReaction(userIds, code)
    }).toSeq
  }

  def getDialogShortDBIO(dialog: Dialog)(implicit ec: ExecutionContext): DBIO[ApiDialogShort] =
    for {
      historyOwner ← DBIO.from(HistoryUtils.getHistoryOwner(dialog.peer, dialog.userId))
      messageOpt ← HistoryMessageRepo.findNewest(historyOwner, dialog.peer) map (_.map(_.ofUser(dialog.userId)))
      unreadCount ← getUnreadCount(dialog.userId, historyOwner, dialog.peer, dialog.ownerLastReadAt)
    } yield ApiDialogShort(
      peer = ApiPeer(ApiPeerType(dialog.peer.typ.value), dialog.peer.id),
      counter = unreadCount,
      date = messageOpt.map(_.date.getMillis).getOrElse(0)
    )

  def getDialogShort(dialog: Dialog)(implicit ec: ExecutionContext): Future[ApiDialogShort] =
    db.run(getDialogShortDBIO(dialog))

  private def processorRegion(peer: Peer): ActorRef = peer.typ match {
    case PeerType.Private ⇒
      userExt.processorRegion.ref //to user peer
    case PeerType.Group ⇒
      groupExt.processorRegion.ref //to group peer
    case _ ⇒ throw new RuntimeException("Unknown peer type!")
  }
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)
}
