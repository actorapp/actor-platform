package im.actor.server.dialog

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.wrappers.Int64Value
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.ApiConversions._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.model._
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Failure

trait DialogCommandHandlers extends PeersImplicits {
  this: DialogProcessor ⇒

  import DialogCommands._
  import DialogEvents._

  protected def sendMessage(s: DialogState, sm: SendMessage): Unit = {
    becomeStashing(replyTo ⇒ ({
      case seq: SeqStateDate ⇒
        persist(NewMessage(sm.randomId, Instant.ofEpochMilli(seq.date), sm.getOrigin.id, sm.message.header)) { e ⇒
          commit(e)
          replyTo ! seq
          unstashAll()
          context.become(receiveCommand)
        }
      case fail: Status.Failure ⇒
        log.error(fail.cause, "Failed to send message")
        replyTo forward fail
        context.become(receiveCommand)
        unstashAll()
    }: Receive) orElse reactions(state), discardOld = true)

    validateAccessHash(sm.getDest, sm.senderAuthId map (_.value), sm.accessHash map (_.value)) map { valid ⇒
      if (valid) {
        withCachedFuture[AuthSidRandomId, SeqStateDate](sm.senderAuthSid → sm.randomId) {
          val sendDate = calcSendDate(state)
          val message = sm.message
          PubSubExtension(system).publish(PeerMessage(sm.getOrigin, sm.getDest, sm.randomId, sendDate, message))
          for {
            _ ← dialogExt.ackSendMessage(peer, sm.copy(date = Some(Int64Value(sendDate))))
            _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sendDate), sm.randomId, message.header, message.toByteArray))
            SeqState(seq, state) ← deliveryExt.senderDelivery(userId, sm.senderAuthSid, peer, sm.randomId, sendDate, message, sm.isFat)
          } yield SeqStateDate(seq, state, sendDate)
        } pipeTo self
      } else {
        self ! Status.Failure(InvalidAccessHash)
      }
    }
  }

  protected def ackSendMessage(s: DialogState, sm: SendMessage): Unit = {
    val messageDate = sm.date getOrElse {
      throw new RuntimeException("No message date found in SendMessage")
    }

    persist(NewMessage(sm.randomId, Instant.ofEpochMilli(messageDate.value), sm.getOrigin.id, sm.message.header)) { e ⇒
      commit(e)

      if (peer.typ == PeerType.Private) {
        SocialManager.recordRelation(sm.getOrigin.id, userId)
        SocialManager.recordRelation(userId, sm.getOrigin.id)
      }

      deliveryExt
        .receiverDelivery(userId, sm.getOrigin.id, peer, sm.randomId, messageDate.value, sm.message, sm.isFat)
        .map(_ ⇒ SendMessageAck())
        .pipeTo(sender())

      deliveryExt.sendCountersUpdate(userId)
    }
  }

  protected def writeMessageSelf(
    s:            DialogState,
    senderUserId: Int,
    dateMillis:   Long,
    randomId:     Long,
    message:      ApiMessage
  ): Unit = {
    if (peer.`type` == PeerType.Private && peer.id != senderUserId && userId != senderUserId) {
      sender() ! Status.Failure(new RuntimeException(s"writeMessageSelf with senderUserId $senderUserId in dialog of user $userId with user ${peer.id}"))
    } else {
      persist(NewMessage(randomId, Instant.ofEpochMilli(dateMillis), senderUserId, message.header)) { e ⇒
        commit(e)
        db.run(writeHistoryMessageSelf(userId, peer, senderUserId, new DateTime(dateMillis), randomId, message.header, message.toByteArray))
          .map(_ ⇒ WriteMessageSelfAck()) pipeTo sender()
      }
    }
  }

  protected def messageReceived(state: DialogState, mr: MessageReceived): Unit = {
    val mustReceive = mustMakeReceive(state, mr)

    if (mustReceive) {
      (for {
        _ ← dialogExt.ackMessageReceived(peer, mr)
      } yield MessageReceivedAck()) pipeTo sender()
    } else {
      sender() ! MessageReceivedAck()
    }
  }

  protected def ackMessageReceived(mr: MessageReceived): Unit = {
    persist(MessagesReceived(Instant.ofEpochMilli(mr.date))) { e ⇒
      commit(e)

      (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender() andThen {
        case Failure(err) ⇒ log.error(err, "Failed to ack MessageReceived")
      }
    }
  }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr)

    if (mustRead) {
      persist(MessagesRead(Instant.ofEpochMilli(mr.date), mr.getOrigin.id)) { e ⇒
        commit(e)

        (for {
          _ ← dialogExt.ackMessageRead(peer, mr)
          unreadCount ← dialogExt.getUnreadTotal(userId)
          _ ← deliveryExt.read(userId, mr.readerAuthSid, peer, mr.date, Some(unreadCount))
          _ = deliveryExt.sendCountersUpdate(userId, unreadCount)
        } yield MessageReadAck()) pipeTo sender()
      }
    } else {
      sender() ! MessageReadAck()
    }
  }

  protected def ackMessageRead(mr: MessageRead): Unit = {
    require(mr.getOrigin.typ.isPrivate)
    persist(MessagesRead(Instant.ofEpochMilli(mr.date), mr.getOrigin.id)) { e ⇒
      commit(e)
      (deliveryExt.notifyRead(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReadAck() }) pipeTo sender() andThen {
        case Failure(err) ⇒ log.error(err, "Failed to ack MessageRead")
      }
    }
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
    } yield SetReactionAck(Some(seqstate), reactions)) pipeTo sender()
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
    } yield RemoveReactionAck(Some(seqstate), reactions)) pipeTo sender()
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
    if (state.lastMessageDate.toEpochMilli == candidate) state.lastMessageDate.toEpochMilli + 1 else candidate
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
    Instant.ofEpochMilli(mr.date).isAfter(state.lastReceiveDate) && (mr.date <= mr.now || state.lastMessageDate.isAfter(Instant.ofEpochMilli(mr.date)))

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
    Instant.ofEpochMilli(mr.date).isAfter(state.lastReadDate) && (mr.date <= mr.now || state.lastMessageDate.isAfter(Instant.ofEpochMilli(mr.date)))

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
