package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import com.google.protobuf.wrappers.{ Int32Value, Int64Value }
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiDialog, ApiDialogGroup, ApiMessage, ApiTextMessage }
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.ApiPeer
import im.actor.extension.InternalExtensions
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogCommands._
import im.actor.server.group.{ GroupEnvelope, GroupExtension }
import im.actor.server.model._
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.sequence.{ SeqState, SeqStateDate, SeqUpdatesExtension }
import im.actor.server.user.{ UserEnvelope, UserExtension }
import im.actor.types._
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension with PeersImplicits {
  import HistoryUtils._

  DialogRoot.register()
  DialogProcessor.register()

  val InternalDialogExtensions = "modules.messaging.extensions"

  private lazy val userExt = UserExtension(system)
  private lazy val groupExt = GroupExtension(system)
  private lazy val seqUpdExt = SeqUpdatesExtension(system)
  private lazy val db = DbExtension(system).db

  private implicit val s: ActorSystem = system
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val timeout: Timeout = Timeout(20.seconds) // TODO: configurable

  private val log = Logging(system, getClass)

  //send message from client
  def sendMessage(
    peer:         ApiPeer,
    senderUserId: UserId,
    senderAuthId: Long,
    randomId:     RandomId,
    message:      ApiMessage,
    accessHash:   Long,
    isFat:        Boolean        = false,
    forUserId:    Option[UserId] = None
  ): Future[SeqStateDate] = {
    val mPeer = peer.asModel
    _sendMessage(
      peer = mPeer,
      senderUserId = senderUserId,
      sendMessage = SendMessage(
        origin = Some(Peer.privat(senderUserId)),
        dest = Some(mPeer),
        senderAuthId = Some(senderAuthId),
        date = None,
        randomId = randomId,
        message = message,
        accessHash = Some(accessHash),
        isFat = isFat,
        forUserId = forUserId
      )
    )
  }

  /**
   * Method used to send messages from bots
   *
   * @note This method should not be used from rpc services,
   * or anywhere else, where we need to validate user's input.
   * Violators will be punished.
   */
  //send message from bot
  def sendMessageInternal(
    peer:         ApiPeer,
    senderUserId: UserId,
    randomId:     RandomId,
    message:      ApiMessage,
    isFat:        Boolean        = false,
    forUserId:    Option[UserId] = None
  ): Future[Unit] = {
    val mPeer = peer.asModel
    _sendMessage(
      peer = mPeer,
      senderUserId = senderUserId,
      sendMessage = SendMessage(
        origin = Some(Peer.privat(senderUserId)),
        dest = Some(peer.asModel),
        senderAuthId = None,
        date = None,
        randomId = randomId,
        message = message,
        accessHash = None,
        isFat = isFat,
        forUserId = forUserId
      )
    ) map (_ ⇒ ())
  }

  /**
   * Send message initiated by server on behalf of `senderUserId`, and return SeqStateDate
   * for `senderAuthId`. The main difference here, is that inside, `UpdateMessage` pushed to
   * all sender's devices(in contrary, usually we push `UpdateMessageSent` to `senderAuthId` device)
   * This is achieved by not passing real authId when sending message
   * We also don't provide accessHash here, cause we trust server.
   *
   * @note This method should only be used when server initiates message sending,
   *       and can't be used when we are dealing with client input(rpc services/http api)
   */
  def sendServerMessage(
    peer:         ApiPeer,
    senderUserId: UserId,
    senderAuthId: Long,
    randomId:     RandomId,
    message:      ApiMessage,
    deliveryTag:  Option[String] = None // tag that we will use to optimize sequence
  ): Future[SeqStateDate] = {
    val mPeer = peer.asModel
    val sendFu = _sendMessage(
      peer = mPeer,
      senderUserId = senderUserId,
      sendMessage = SendMessage(
        origin = Some(Peer.privat(senderUserId)),
        dest = Some(mPeer),
        senderAuthId = None, // don't pass authId - so we can deliver UpdateMessage to all authIds of sender user.
        date = None,
        randomId = randomId,
        message = message,
        accessHash = None, // don't pass accessHash - we trust server
        deliveryTag = deliveryTag
      )
    )

    for {
      seqStateDate ← sendFu
      SeqState(seq, state) ← seqUpdExt.getSeqState(senderUserId, senderAuthId)
    } yield seqStateDate.copy(seq = seq, state = state)
  }

  def ackSendMessage(peer: Peer, sm: SendMessage): Future[SendMessageAck] =
    (processorRegion(peer) ? envelope(peer, DialogEnvelope().withSendMessage(sm))).mapTo[SendMessageAck]

  def writeMessage(
    peer:         ApiPeer,
    senderUserId: Int,
    date:         Instant,
    randomId:     Long,
    message:      ApiMessage
  ): Future[Unit] =
    withValidPeer(peer.asModel, senderUserId, failed = FastFuture.successful(())) {
      for {
        memberIds ← fetchMemberIds(DialogId(peer.asModel, senderUserId))
        _ ← Future.sequence(memberIds map (writeMessageSelf(_, peer, senderUserId, new DateTime(date.toEpochMilli), randomId, message)))
      } yield ()
    }

  def writeMessageSelf(
    userId:       Int,
    peer:         ApiPeer,
    senderUserId: Int,
    date:         DateTime,
    randomId:     Long,
    message:      ApiMessage
  ): Future[Unit] =
    withValidPeer(Peer.privat(userId), peer.id, failed = FastFuture.successful(())) {
      (userExt.processorRegion.ref ?
        envelope(Peer.privat(userId), DialogEnvelope().withWriteMessageSelf(WriteMessageSelf(
          dest = Some(peer.asModel),
          senderUserId,
          date.getMillis,
          randomId,
          message
        )))) map (_ ⇒ ())
    }

  def messageReceived(peer: ApiPeer, receiverUserId: Int, date: Long): Future[Unit] =
    withValidPeer(peer.asModel, receiverUserId, failed = FastFuture.successful(())) {
      val now = Instant.now().toEpochMilli
      val receiver = Peer.privat(receiverUserId)
      val messageReceived = MessageReceived(Some(receiver), Some(peer.asModel), date, now)
      (userExt.processorRegion.ref ? envelope(receiver, DialogEnvelope().withMessageReceived(messageReceived)))
        .mapTo[MessageReceivedAck] map (_ ⇒ ())
    }

  def ackMessageReceived(peer: Peer, mr: MessageReceived): Future[Unit] =
    (processorRegion(peer) ? envelope(peer, DialogEnvelope().withMessageReceived(mr)))
      .mapTo[MessageReceivedAck] map (_ ⇒ ())

  def messageRead(peer: ApiPeer, readerUserId: Int, readerAuthId: Long, date: Long): Future[Unit] =
    withValidPeer(peer.asModel, readerUserId, failed = FastFuture.successful(())) {
      val now = Instant.now().toEpochMilli
      val reader = Peer.privat(readerUserId)
      val messageRead = MessageRead(Some(reader), Some(peer.asModel), readerAuthId, date, now)
      (userExt.processorRegion.ref ? envelope(reader, DialogEnvelope().withMessageRead(messageRead)))
        .mapTo[MessageReadAck] map (_ ⇒ ())
    }

  def ackMessageRead(peer: Peer, mr: MessageRead): Future[Unit] =
    (processorRegion(peer) ? envelope(peer, DialogEnvelope().withMessageRead(mr)))
      .mapTo[MessageReadAck] map (_ ⇒ ())

  // DialogRootOperations
  def unarchive(userId: Int, clientAuthId: Long, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, failed = Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ?
        UserEnvelope(userId)
        .withDialogRootEnvelope(
          DialogRootEnvelope().withUnarchive(DialogRootCommands.Unarchive(Some(peer), clientAuthId))
        ))
        .mapTo[SeqState]
    }

  def archive(userId: Int, clientAuthId: Long, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, failed = Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ?
        UserEnvelope(userId)
        .withDialogRootEnvelope(
          DialogRootEnvelope().withArchive(DialogRootCommands.Archive(Some(peer), clientAuthId))
        ))
        .mapTo[SeqState]
    }

  def favourite(userId: Int, clientAuthId: Long, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, failed = Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ?
        UserEnvelope(userId)
        .withDialogRootEnvelope(
          DialogRootEnvelope().withFavourite(DialogRootCommands.Favourite(Some(peer), clientAuthId))
        ))
        .mapTo[SeqState]
    }

  def unfavourite(userId: Int, clientAuthId: Long, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId, failed = Future.failed[SeqState](DialogErrors.MessageToSelf)) {
      (userExt.processorRegion.ref ?
        UserEnvelope(userId)
        .withDialogRootEnvelope(DialogRootEnvelope().withUnfavourite(DialogRootCommands.Unfavourite(Some(peer), clientAuthId))))
        .mapTo[SeqState]
    }

  def delete(userId: Int, clientAuthId: Long, peer: Peer): Future[SeqState] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ?
        UserEnvelope(userId).withDialogRootEnvelope(DialogRootEnvelope().withDelete(DialogRootCommands.Delete(Some(peer), clientAuthId))))
        .mapTo[SeqState]
    }

  def setReaction(userId: Int, authId: Long, peer: Peer, randomId: Long, code: String): Future[SetReactionAck] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ? UserEnvelope(userId).withDialogEnvelope(DialogEnvelope().withSetReaction(SetReaction(
        origin = Some(Peer.privat(userId)),
        dest = Some(peer),
        clientAuthId = authId,
        randomId = randomId,
        code = code
      )))).mapTo[SetReactionAck]
    }

  def removeReaction(userId: Int, authId: Long, peer: Peer, randomId: Long, code: String): Future[RemoveReactionAck] =
    withValidPeer(peer, userId) {
      (userExt.processorRegion.ref ? UserEnvelope(userId).withDialogEnvelope(DialogEnvelope().withRemoveReaction(RemoveReaction(
        origin = Some(Peer.privat(userId)),
        dest = Some(peer),
        clientAuthId = authId,
        randomId = randomId,
        code = code
      )))).mapTo[RemoveReactionAck]
    }

  def ackSetReaction(peer: Peer, sr: SetReaction): Future[Unit] =
    (processorRegion(peer) ? envelope(peer, DialogEnvelope().withSetReaction(sr))) map (_ ⇒ ())

  def ackRemoveReaction(peer: Peer, rr: RemoveReaction): Future[Unit] =
    (processorRegion(peer) ? envelope(peer, DialogEnvelope().withRemoveReaction(rr))) map (_ ⇒ ())

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

  def getUnreadTotal(userId: Int): Future[Int] =
    (processorRegion(Peer.privat(userId)) ?
      UserEnvelope(userId)
      .withDialogRootEnvelope(
        DialogRootEnvelope()
          .withGetCounter(DialogRootQueries.GetCounter())
      ))
      .mapTo[DialogRootQueries.GetCounterResponse] map (_.counter)

  def isSharedUser(userId: Int): Boolean = userId == 0

  def fetchGroupedDialogs(userId: Int): Future[Seq[DialogGroup]] = {
    (processorRegion(Peer.privat(userId)) ?
      UserEnvelope(userId)
      .withDialogRootEnvelope(DialogRootEnvelope()
        .withGetDialogGroups(DialogRootQueries.GetDialogGroups())))
      .mapTo[DialogRootQueries.GetDialogGroupsResponse]
      .map(_.groups)
  }

  def fetchApiGroupedDialogs(userId: Int): Future[Vector[ApiDialogGroup]] = {
    fetchGroupedDialogs(userId) map { groups ⇒
      groups.toVector map (_.asStruct)
    }
  }

  def fetchDialogs(userId: Int, endDate: Instant, limit: Int): Future[Map[Instant, DialogInfo]] = {
    (processorRegion(Peer.privat(userId)) ?
      UserEnvelope(userId)
      .withDialogRootEnvelope(DialogRootEnvelope()
        .withGetDialogs(DialogRootQueries.GetDialogs(endDate, limit))))
      .mapTo[DialogRootQueries.GetDialogsResponse]
      .map(_.dialogs.map { case (date, dialog) ⇒ Instant.ofEpochMilli(date) → dialog })
  }

  def fetchApiDialogs(userId: Int, endDate: Instant, limit: Int): Future[Iterable[ApiDialog]] = {
    for {
      infos ← fetchDialogs(userId, endDate, limit)
      dialogs ← Future.sequence(infos map { case (date, info) ⇒ getApiDialog(userId, info, date) })
    } yield dialogs
  }

  def fetchArchivedDialogs(userId: Int, offset: Option[Array[Byte]], limit: Int): Future[(Map[Instant, DialogInfo], Option[Array[Byte]])] = {
    (processorRegion(Peer.privat(userId)) ?
      UserEnvelope(userId)
      .withDialogRootEnvelope(DialogRootEnvelope()
        .withGetArchivedDialogs(DialogRootQueries.GetArchivedDialogs(offset map (b ⇒ Int64Value.parseFrom(b).value), limit))))
      .mapTo[DialogRootQueries.GetArchivedDialogsResponse]
      .map {
        case DialogRootQueries.GetArchivedDialogsResponse(dialogs, nextOffset) ⇒
          (
            dialogs.map { case (date, dialog) ⇒ Instant.ofEpochMilli(date) → dialog },
            nextOffset map (o ⇒ Int64Value(o).toByteArray)
          )
      }
  }

  def fetchArchivedApiDialogs(userId: Int, offset: Option[Array[Byte]], limit: Int): Future[(Iterable[ApiDialog], Option[Array[Byte]])] = {
    for {
      (infos, nextOffset) ← fetchArchivedDialogs(userId, offset, limit)
      dialogs ← Future.sequence(infos map { case (date, info) ⇒ getApiDialog(userId, info, date) })
    } yield (dialogs, nextOffset)
  }

  def getDialogInfo(userId: Int, peer: Peer): Future[DialogInfo] = {
    (userExt.processorRegion.ref ? UserEnvelope(userId).withDialogEnvelope(DialogEnvelope().withGetInfo(DialogQueries.GetInfo(Some(peer)))))
      .mapTo[DialogQueries.GetInfoResponse]
      .map(_.info.get)
  }

  def getApiDialog(userId: Int, info: DialogInfo, sortDate: Instant): Future[ApiDialog] = {
    val emptyMessageContent = ApiTextMessage(text = "", mentions = Vector.empty, ext = None)
    val emptyMessage = HistoryMessage(userId, info.peer.get, new DateTime(0), 0, 0, emptyMessageContent.header, emptyMessageContent.toByteArray, None)
    val peer = info.peer.get

    for {
      historyOwner ← getHistoryOwner(peer, userId)
      messageOpt ← getLastMessage(userId, peer)
      reactions ← messageOpt map (m ⇒ db.run(fetchReactions(peer, userId, m.randomId))) getOrElse FastFuture.successful(Vector.empty)
      message ← getLastMessage(userId, peer) map (_ getOrElse emptyMessage) map (_.asStruct(
        lastReceivedAt = new DateTime(info.lastReceivedDate.toEpochMilli),
        lastReadAt = new DateTime(info.lastReadDate.toEpochMilli),
        reactions = reactions,
        attributes = None
      )) map (_.getOrElse(throw new RuntimeException("Failed to get message struct")))
      firstUnreadOpt ← db.run(HistoryMessageRepo.findAfter(historyOwner, peer, new DateTime(info.lastReadDate.toEpochMilli), 1) map (_.headOption map (_.ofUser(userId))))
    } yield ApiDialog(
      peer = peer.asStruct,
      unreadCount = info.counter,
      sortDate = sortDate.toEpochMilli,
      senderUserId = message.senderUserId,
      randomId = message.randomId,
      date = message.date,
      message = message.message,
      state = message.state,
      firstUnreadDate = firstUnreadOpt map (_.date.getMillis),
      attributes = None
    )
  }

  def fetchReactions(peer: Peer, clientUserId: Int, randomId: Long): DBIO[Seq[MessageReaction]] =
    ReactionEventRepo.fetch(DialogId(peer, clientUserId), randomId) map reactions

  def fetchReactions(peer: Peer, clientUserId: Int, randomIds: Set[Long]): DBIO[Map[Long, Seq[MessageReaction]]] =
    for {
      events ← ReactionEventRepo.fetch(DialogId(peer, clientUserId), randomIds)
    } yield events.view.groupBy(_.randomId).mapValues(reactions)

  private def _sendMessage(peer: Peer, senderUserId: Int, sendMessage: SendMessage) =
    withValidPeer(peer, senderUserId, failed = FastFuture.successful(SeqStateDate())) {
      (userExt.processorRegion.ref ?
        UserEnvelope(senderUserId)
        .withDialogEnvelope(DialogEnvelope().withSendMessage(sendMessage))).mapTo[SeqStateDate]
    }

  private def getLastMessage(userId: Int, peer: Peer): Future[Option[HistoryMessage]] =
    db.run(HistoryMessageRepo.findNewest(userId, peer))

  private def reactions(events: Seq[ReactionEvent]): Seq[MessageReaction] = {
    (events.view groupBy (_.code) mapValues (_ map (_.userId)) map {
      case (code, userIds) ⇒ MessageReaction(userIds, code)
    }).toSeq
  }

  private def withValidPeer[A](peer: Peer, senderUserId: Int, failed: ⇒ Future[A] = Future.failed[A](DialogErrors.MessageToSelf))(f: ⇒ Future[A]): Future[A] =
    peer match {
      case Peer(PeerType.Private, id) if id == senderUserId ⇒
        log.error(s"Attempt to work with yourself, userId: $senderUserId")
        failed
      case _ ⇒ f
    }

  private def fetchMemberIds(dialogId: DialogId): Future[Set[Int]] = {
    dialogId match {
      case p: PrivateDialogId ⇒ FastFuture.successful(Set(p.id1, p.id2))
      case g: GroupDialogId   ⇒ groupExt.getMemberIds(g.groupId) map (_._1.toSet)
    }
  }

  private def processorRegion(peer: Peer): ActorRef = peer.typ match {
    case PeerType.Private ⇒
      userExt.processorRegion.ref // to user peer
    case PeerType.Group ⇒
      groupExt.processorRegion.ref // to group peer
    case _ ⇒ throw new RuntimeException("Unknown peer type!")
  }

  private def envelope(peer: Peer, envelope: DialogEnvelope): Any = peer.typ match {
    case PeerType.Private ⇒ UserEnvelope(peer.id).withDialogEnvelope(envelope)
    case PeerType.Group   ⇒ GroupEnvelope(peer.id).withDialogEnvelope(envelope)
    case _                ⇒ throw new RuntimeException("Unknown peer type")
  }
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)

  def groupKey(group: DialogGroupType) = group match {
    case DialogGroupType.Favourites     ⇒ "favourites"
    case DialogGroupType.DirectMessages ⇒ "privates"
    case DialogGroupType.Groups         ⇒ "groups"
    case unknown                        ⇒ throw DialogErrors.UnknownDialogGroupType(unknown)
  }

  def groupTitle(group: DialogGroupType) = group match {
    case DialogGroupType.Favourites     ⇒ "Favourites"
    case DialogGroupType.DirectMessages ⇒ "Direct Messages"
    case DialogGroupType.Groups         ⇒ "Groups"
    case unknown                        ⇒ throw DialogErrors.UnknownDialogGroupType(unknown)
  }
}
