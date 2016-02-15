package im.actor.server.dialog

import java.time.Instant

import akka.actor.{ ActorRef, PoisonPill, Status }
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.ApiConversions._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model._
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Failure

trait DialogCommandHandlers extends UpdateCounters with PeersImplicits {
  this: DialogProcessor ⇒

  import DialogCommands._
  import DialogEvents._

  protected def sendMessage(s: DialogState, sm: SendMessage): Unit = {
    withCreated(s) { state ⇒
      becomeStashing(replyTo ⇒ ({
        case seq: SeqStateDate ⇒
          replyTo ! seq
          if (state.isArchived) {
            self.tell(Show(peer), ActorRef.noSender)
          }
          updateMessageDate(state, seq.date)
          unstashAll()
        case fail: Status.Failure ⇒
          log.error(fail.cause, "Failed to send message")
          replyTo forward fail
          context unbecome ()
          unstashAll()
      }: Receive) orElse reactions(state), discardOld = false)

      validateAccessHash(sm.dest, sm.senderAuthId, sm.accessHash) map { valid ⇒
        if (valid) {
          withCachedFuture[AuthSidRandomId, SeqStateDate](sm.senderAuthSid → sm.randomId) {
            val sendDate = calcSendDate(state)
            val message = sm.message
            PubSubExtension(system).publish(PeerMessage(sm.origin, sm.dest, sm.randomId, sendDate, message))
            for {
              _ ← dialogExt.ackSendMessage(peer, sm.copy(date = Some(sendDate)))
              _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sendDate), sm.randomId, message.header, message.toByteArray))
              _ ← dialogExt.updateCounters(peer, userId)
              SeqState(seq, state) ← deliveryExt.senderDelivery(userId, sm.senderAuthSid, peer, sm.randomId, sendDate, message, sm.isFat)
            } yield SeqStateDate(seq, state, sendDate)
          } pipeTo self
        } else {
          self ! Status.Failure(InvalidAccessHash)
        }
      }
    }
  }

  protected def updateCountersChanged(): Unit = {
    deliveryExt.sendCountersUpdate(userId)
      .map(_ ⇒ SendMessageAck())
      .pipeTo(sender())
  }

  protected def ackSendMessage(s: DialogState, sm: SendMessage): Unit =
    withCreated(s) { state ⇒
      val messageDate = sm.date getOrElse { throw new RuntimeException("No message date found in SendMessage") }
      if (peer.typ == PeerType.Private) {
        SocialManager.recordRelation(sm.origin.id, userId)
        SocialManager.recordRelation(userId, sm.origin.id)
      }

      deliveryExt
        .receiverDelivery(userId, sm.origin.id, peer, sm.randomId, messageDate, sm.message, sm.isFat)
        .map(_ ⇒ SendMessageAck())
        .pipeTo(sender()) onSuccess {
          case _ ⇒
            if (state.isArchived) { self.tell(Show(peer), ActorRef.noSender) }
        }
    }

  protected def writeMessage(
    s:          DialogState,
    dateMillis: Long,
    randomId:   Long,
    message:    ApiMessage
  ): Unit =
    withCreated(s) { _ ⇒
      val date = new DateTime(dateMillis)

      db.run(writeHistoryMessage(
        selfPeer,
        peer,
        date,
        randomId,
        message.header,
        message.toByteArray
      )) map (_ ⇒ WriteMessageAck()) pipeTo sender()
    }

  protected def writeMessageSelf(
    s:            DialogState,
    senderUserId: Int,
    dateMillis:   Long,
    randomId:     Long,
    message:      ApiMessage
  ): Unit =
    withCreated(s) { _ ⇒
      val result =
        if (peer.`type` == PeerType.Private && peer.id != senderUserId && userId != senderUserId) {
          Future.failed(new RuntimeException(s"writeMessageSelf with senderUserId $senderUserId in dialog of user $userId with user ${peer.id}"))
        } else {
          db.run(writeHistoryMessageSelf(userId, peer, senderUserId, new DateTime(dateMillis), randomId, message.header, message.toByteArray))
        }

      result map (_ ⇒ WriteMessageSelfAck()) pipeTo sender()
    }

  protected def messageReceived(state: DialogState, mr: MessageReceived): Unit = {
    val mustReceive = mustMakeReceive(state, mr)
    (if (mustReceive) {
      for {
        _ ← dialogExt.ackMessageReceived(peer, mr)
        _ ← db.run(markMessagesReceived(selfPeer, peer, new DateTime(mr.date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to process MessageReceived")
    }

    if (mustReceive) {
      updateReceiveDate(state, mr.date)
    }
  }

  protected def ackMessageReceived(mr: MessageReceived): Unit = {
    (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to ack MessageReceived")
    }
  }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr)

    val readerUpd = deliveryExt.read(userId, mr.readerAuthSid, peer, mr.date)
    (if (mustRead) {
      for {
        _ ← readerUpd
        _ ← dialogExt.ackMessageRead(peer, mr)
        _ ← db.run(markMessagesRead(selfPeer, peer, new DateTime(mr.date)))
        _ ← deliveryExt.sendCountersUpdate(userId)
      } yield MessageReadAck()
    } else {
      Future.successful(MessageReadAck())
    }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to process MessageRead")
    }

    if (mustRead) {
      updateReadDate(state, mr.date)
    }
  }

  protected def ackMessageRead(mr: MessageRead): Unit =
    (deliveryExt.notifyRead(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReadAck() }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to ack MessageRead")
    }

  protected def archive(state: DialogState): Unit = {
    if (state.isArchived)
      sender ! Status.Failure(DialogErrors.DialogAlreadyArchived(peer))
    else {
      val future =
        (for {
          _ ← db.run(DialogRepo.archive(userId, peer))
          _ ← db.run(markMessagesRead(selfPeer, peer, new DateTime))
          _ ← userExt.notifyDialogsChanged(userId)
          seqstate ← seqUpdExt.deliverSingleUpdate(userId, UpdateChatArchive(peer.asStruct))
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateArchived(state)
      }
    }
  }

  protected def show(state: DialogState): Unit = {
    if (!state.isArchived)
      sender ! Status.Failure(DialogErrors.DialogAlreadyShown(peer))
    else {
      val future =
        (for {
          _ ← db.run(DialogRepo.show(userId, peer))
          seqstate ← userExt.notifyDialogsChanged(userId)
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateShown(state)
      }
    }
  }

  protected def favourite(state: DialogState): Unit = {
    if (state.isFavourite)
      sender ! Status.Failure(DialogErrors.DialogAlreadyFavourited(peer))
    else {
      val future =
        (for {
          _ ← db.run(DialogRepo.favourite(userId, peer))
          seqstate ← userExt.notifyDialogsChanged(userId)
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateFavourited(state)
      }
    }
  }

  protected def unfavourite(state: DialogState): Unit = {
    if (!state.isFavourite)
      sender ! Status.Failure(DialogErrors.DialogAlreadyUnfavourited(peer))
    else {
      val future =
        (for {
          _ ← db.run(DialogRepo.unfavourite(userId, peer))
          seqstate ← userExt.notifyDialogsChanged(userId)
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateUnfavourited(state)
      }
    }
  }

  protected def delete(state: DialogState): Unit = {
    val update = UpdateChatDelete(peer.asStruct)

    val future =
      for {
        _ ← db.run(
          HistoryMessageRepo.deleteAll(userId, peer)
            andThen DialogRepo.delete(userId, peer)
        )
        _ ← userExt.notifyDialogsChanged(userId)
        seqstate ← seqUpdExt.deliverSingleUpdate(userId, update)
      } yield seqstate

    future pipeTo sender() onSuccess { case _ ⇒ self ! PoisonPill }
  }

  protected def setReaction(state: DialogState, sr: SetReaction): Unit = {
    (for {
      reactions ← db.run {
        ReactionEventRepo.create(DialogId(peer, userId), sr.randomId, sr.code, userId)
          .andThen(dialogExt.fetchReactions(peer, userId, sr.randomId))
      }
      seqstate ← seqUpdExt.deliverSingleUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, sr.randomId, reactions.toVector)
      )
      _ ← dialogExt.ackSetReaction(peer, sr)
    } yield SetReactionAck(seqstate, reactions)) pipeTo sender()
  }

  protected def ackSetReaction(sr: SetReaction): Unit = {
    (for {
      reactions ← db.run(dialogExt.fetchReactions(peer, userId, sr.randomId))
      _ ← seqUpdExt.deliverSingleUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, sr.randomId, reactions.toVector)
      )
    } yield SetReactionAck()) pipeTo sender()
  }

  protected def removeReaction(state: DialogState, rr: RemoveReaction): Unit = {
    (for {
      reactions ← db.run {
        ReactionEventRepo.delete(DialogId(peer, userId), rr.randomId, rr.code, userId)
          .andThen(dialogExt.fetchReactions(peer, userId, rr.randomId))
      }
      seqstate ← seqUpdExt.deliverSingleUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, rr.randomId, reactions.toVector)
      )
      _ ← dialogExt.ackRemoveReaction(peer, rr)
      _ ← dialogExt.ackRemoveReaction(peer, rr)
    } yield RemoveReactionAck(seqstate, reactions)) pipeTo sender()
  }

  protected def ackRemoveReaction(rr: RemoveReaction): Unit = {
    (for {
      reactions ← db.run(dialogExt.fetchReactions(peer, userId, rr.randomId))
      _ ← seqUpdExt.deliverSingleUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, rr.randomId, reactions.toVector)
      )
    } yield RemoveReactionAck()) pipeTo sender()
  }

  /**
   * Yields unique message date in current dialog.
   * When `candidate` date is same as last message date, we increment `candidate` value by 1,
   * thus resulting date can possibly be in future
   *
   * @param state current dialog state
   * @return unique message date in current dialog
   */
  private def calcSendDate(state: DialogState): Long = {
    val candidate = Instant.now.toEpochMilli
    if (state.lastMessageDate == candidate) state.lastMessageDate + 1 else candidate
  }

  /**
   *
   * For performance purposes, we have to avoid processing duplicated receive requests(requests with same `date`)
   * We also must validate receive date - it should not be in future - otherwise it will break processing of
   * subsequent receive requests with correct `date`
   *
   * Valid receive date must be:
   * • greater than current last receive date
   * • less or equal than current date(`now`), or less or equal than last message date.
   *
   * @param state current dialog state
   * @param mr message received request from client
   * @return `true` if we must process message received request and `false` otherwise
   */
  private def mustMakeReceive(state: DialogState, mr: MessageReceived): Boolean =
    (mr.date > state.lastReceiveDate) && (mr.date <= mr.now || mr.date <= state.lastMessageDate)

  /**
   *
   * For performance purposes, we have to avoid processing duplicated read requests(requests with same `date`)
   * We also must validate read date - it should not be in future - otherwise it will break processing of
   * subsequent read requests with correct `date`
   *
   * Valid read date must be:
   * • greater than current last read date
   * • less or equal than current date(`now`), or less or equal than last message date.
   *
   * @param state current dialog state
   * @param mr message received request from client
   * @return `true` if we must process message received request and `false` otherwise
   */
  private def mustMakeRead(state: DialogState, mr: MessageRead): Boolean =
    (mr.date > state.lastReadDate) && (mr.date <= mr.now || mr.date <= state.lastMessageDate)

  protected def updateMessageDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastMessageDate(date)))

  private def updateReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReceiveDate(date)))

  private def updateReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReadDate(date)))

  private def updateArchived(state: DialogState): Unit =
    context become initialized(state.updated(Archived))

  private def updateShown(state: DialogState): Unit =
    context become initialized(state.updated(Shown))

  private def updateFavourited(state: DialogState): Unit =
    context become initialized(state.updated(Favourited))

  private def updateUnfavourited(state: DialogState): Unit =
    context become initialized(state.updated(Unfavourited))

  /**
   * check access hash
   * If `optAccessHash` is `None` - we simply don't check access hash
   * If `optSenderAuthId` is None, and we are validating access hash for private peer - it is invalid
   */
  private def validateAccessHash(peer: Peer, optSenderAuthId: Option[Long], optAccessHash: Option[Long]): Future[Boolean] =
    optAccessHash map { hash ⇒
      peer.`type` match {
        case PeerType.Private ⇒
          optSenderAuthId map { authId ⇒ userExt.checkAccessHash(peer.id, authId, hash) } getOrElse Future.successful(false)
        case PeerType.Group ⇒
          groupExt.checkAccessHash(peer.id, hash)
        case unknown ⇒ throw new RuntimeException(s"Unknown peer type $unknown")
      }
    } getOrElse Future.successful(true)
}
