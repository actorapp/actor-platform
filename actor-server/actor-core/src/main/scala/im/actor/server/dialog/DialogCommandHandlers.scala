package im.actor.server.dialog

import java.time.Instant

import akka.actor.Status
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.ApiConversions._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.model._
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Failure

trait DialogCommandHandlers extends PeersImplicits with UserAcl {
  this: DialogProcessor ⇒

  import DialogCommands._
  import DialogEvents._

  protected def sendMessage(sm: SendMessage): Unit = {
    //TODO: add stashing timeout
    becomeStashing(replyTo ⇒ ({
      case seq: SeqStateDate ⇒
        persist(NewMessage(sm.randomId, seq.date, sm.getOrigin.id, sm.message.header)) { e ⇒
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
    }: Receive) orElse reactions, discardOld = true)

    val optClientAuthId = sm.senderAuthId
    withValidAccessHash(sm.getDest, optClientAuthId, sm.accessHash) {
      withCachedFuture[AuthIdRandomId, SeqStateDate](optClientAuthId.getOrElse(0L) → sm.randomId) {
        val sendDate = calcSendDate()
        val message = sm.message
        PubSubExtension(system).publish(PeerMessage(sm.getOrigin, sm.getDest, sm.randomId, sendDate, message))

        for {
          exists ← db.run(HistoryMessageRepo.existstWithRandomId(userId, peer, sm.randomId))
          seqStateDate ← exists match {
            case true ⇒ FastFuture.failed(NotUniqueRandomId)
            case false ⇒
              withNonBlockedPeer[SeqStateDate](userId, sm.getDest)(
                default = for {
                _ ← dialogExt.ackSendMessage(peer, sm.copy(date = Some(sendDate)))
                _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sendDate), sm.randomId, message.header, message.toByteArray))
                //_ = dialogExt.updateCounters(peer, userId)
                SeqState(seq, state) ← deliveryExt.senderDelivery(userId, optClientAuthId, peer, sm.randomId, sendDate, message, sm.isFat, sm.deliveryTag)
              } yield SeqStateDate(seq, state, sendDate),
                failed = for {
                _ ← db.run(writeHistoryMessageSelf(userId, peer, userId, new DateTime(sendDate), sm.randomId, message.header, message.toByteArray))
                SeqState(seq, state) ← deliveryExt.senderDelivery(userId, optClientAuthId, peer, sm.randomId, sendDate, message, sm.isFat, sm.deliveryTag)
              } yield SeqStateDate(seq, state, sendDate)
              )
          }
        } yield seqStateDate
      }
    }
  }

  protected def ackSendMessage(sm: SendMessage): Unit = {
    val messageDate = sm.date getOrElse {
      throw new RuntimeException("No message date found in SendMessage")
    }

    persistAsync(NewMessage(sm.randomId, messageDate, sm.getOrigin.id, sm.message.header)) { e ⇒
      commit(e)

      if (peer.typ == PeerType.Private) {
        SocialManager.recordRelation(sm.getOrigin.id, userId)
        SocialManager.recordRelation(userId, sm.getOrigin.id)
      }

      deliveryExt
        .receiverDelivery(userId, sm.getOrigin.id, peer, sm.randomId, messageDate, sm.message, sm.isFat, sm.deliveryTag)
        .map(_ ⇒ SendMessageAck())
        .pipeTo(sender())

      deliveryExt.sendCountersUpdate(userId)
    }
  }

  protected def writeMessageSelf(
    senderUserId: Int,
    dateMillis:   Long,
    randomId:     Long,
    message:      ApiMessage
  ): Unit = {
    if (peer.`type` == PeerType.Private && peer.id != senderUserId && userId != senderUserId) {
      sender() ! Status.Failure(new RuntimeException(s"writeMessageSelf with senderUserId $senderUserId in dialog of user $userId with user ${peer.id}"))
    } else {
      persist(NewMessage(randomId, dateMillis, senderUserId, message.header)) { e ⇒
        commit(e)
        db.run(writeHistoryMessageSelf(userId, peer, senderUserId, new DateTime(dateMillis), randomId, message.header, message.toByteArray))
          .map(_ ⇒ WriteMessageSelfAck()) pipeTo sender()
      }
    }
  }

  protected def messageReceived(mr: MessageReceived): Unit = {
    val mustReceive = mustMakeReceive(mr)

    if (mustReceive) {
      (for {
        _ ← dialogExt.ackMessageReceived(peer, mr)
      } yield MessageReceivedAck()) pipeTo sender()
    } else {
      sender() ! MessageReceivedAck()
    }
  }

  protected def ackMessageReceived(mr: MessageReceived): Unit = {
    persistAsync(MessagesReceived(mr.date)) { e ⇒
      commit(e)

      (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender() andThen {
        case Failure(err) ⇒ log.error(err, "Failed to ack MessageReceived")
      }
    }
  }

  // я читаю
  protected def messageRead(mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(mr)
    log.debug(s"mustRead is ${mustRead}")

    if (mustRead) {
      // смотрим что в стейте есть даты сообщений до даты прочитки, значит мы можем их прочитать!
      // if Vector - make sort
      // read date is still in unreadMessagesTimestamp cache. we can count them
      // TODO: нужно делать проверку на не пустую коллекцию. иначе будем проваливаться
      if (state.unreadTimestamps.isEmpty || state.unreadTimestamps.exists(_ <= mr.date)) { // зачем сортировать, если мы все равно этим не пользуемся, так ведь?
        // can't use persistAsync here, cause more reads can occur,
        // and state will be inconsistent by the time handler is executed.
        persist(MessagesRead(mr.date, mr.getOrigin.id)) { e ⇒
          log.debug(s"persisted MessagesRead, origin=${mr.getOrigin.id}, date=${Instant.ofEpochMilli(mr.date)}, counter=${state.counter}, unreadMessages=${state.unreadTimestamps}")
          val newState = commit(e)
          log.debug(s"after commit: counter=${newState.counter}, unreadMessages=${newState.unreadTimestamps}")

          ///////////// move to method maybe
          (for {
            _ ← dialogExt.ackMessageRead(peer, mr)
            _ ← deliveryExt.read(userId, mr.readerAuthId, peer, mr.date, newState.counter)
            _ = deliveryExt.sendCountersUpdate(userId)
          } yield MessageReadAck()) pipeTo sender()
          ///////////// move to method
        }
      } else {
        // эти случаи должны происходить оооочень редко, поскольку у нас либа прочитывает весь список сразу
        // но если они происходят, то нужно найти количество сообщений начиная

        // переходим в стешинг, waiting to read from database
        //TODO: add stashing timeout
        becomeStashing(replyTo ⇒ ({
          case evt: UnreadsUpdated ⇒
            persist(evt) { e ⇒
              val newState = commit(e)
              ///////////// move to method maybe
              (for {
                _ ← dialogExt.ackMessageRead(peer, mr)
                _ ← deliveryExt.read(userId, mr.readerAuthId, peer, mr.date, newState.counter)
                _ = deliveryExt.sendCountersUpdate(userId)
              } yield MessageReadAck()) pipeTo replyTo
              /////////////
              context.become(receiveCommand)
              unstashAll()
            }
          case fail: Status.Failure ⇒
            log.error(fail.cause, "Failed to read message")
            replyTo forward fail
            context.become(receiveCommand)
            unstashAll()
        }: Receive) orElse reactions, discardOld = true)

        val sortedUnreads = state.unreadTimestamps
        val earliestStateUnreadDate = sortedUnreads.head
        val unreadCountDb = db.run(
          HistoryMessageRepo.countBetween(
            userId,
            peer,
            new DateTime(mr.date),
            new DateTime(earliestStateUnreadDate)
          )
        )

        //                    [------------------------------------------] - новый список непрочитанных. Нам нужен только count
        // ---------------------------------------[======================]
        //                    ^                   ^                      ^
        //               "Read Date"        "State first date"     "State last date"
        // нужна выборка с "Read Date" по "State first date" - это будут db-unread
        // newCounter = db-unread + state.length
        val result: Future[UnreadsUpdated] = unreadCountDb map { count ⇒
          UnreadsUpdated(
            newReadDate = mr.date,
            newCounter = state.unreadTimestamps.length + count
          )
        }
        result pipeTo self
      }
    } else {
      sender() ! MessageReadAck()
    }
  }

  // меня читают
  protected def ackMessageRead(mr: MessageRead): Unit = {
    require(mr.getOrigin.typ.isPrivate)
    persistAsync(MessagesRead(mr.date, mr.getOrigin.id)) { e ⇒
      commit(e)
      log.debug(s"=== new lastReadDate is ${state.lastReadDate}")
      (deliveryExt.notifyRead(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReadAck() }) pipeTo sender() andThen {
        case Failure(err) ⇒ log.error(err, "Failed to ack MessageRead")
      }
    }
  }

  protected def setReaction(sr: SetReaction): Unit = {
    (for {
      reactions ← db.run {
        ReactionEventRepo.create(DialogId(peer, userId), sr.randomId, sr.code, userId)
          .andThen(dialogExt.fetchReactions(peer, userId, sr.randomId))
      }
      seqState ← seqUpdExt.deliverClientUpdate(
        userId = userId,
        authId = sr.clientAuthId,
        update = UpdateReactionsUpdate(peer.asStruct, sr.randomId, reactions.toVector)
      )
      _ ← dialogExt.ackSetReaction(peer, sr)
    } yield SetReactionAck(Some(seqState), reactions)) pipeTo sender()
  }

  protected def ackSetReaction(sr: SetReaction): Unit = {
    (for {
      reactions ← db.run(dialogExt.fetchReactions(peer, userId, sr.randomId))
      _ ← seqUpdExt.deliverUserUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, sr.randomId, reactions.toVector)
      )
    } yield SetReactionAck()) pipeTo sender()
  }

  protected def removeReaction(rr: RemoveReaction): Unit = {
    (for {
      reactions ← db.run {
        ReactionEventRepo.delete(DialogId(peer, userId), rr.randomId, rr.code, userId)
          .andThen(dialogExt.fetchReactions(peer, userId, rr.randomId))
      }
      seqState ← seqUpdExt.deliverClientUpdate(
        userId = userId,
        authId = rr.clientAuthId,
        update = UpdateReactionsUpdate(peer.asStruct, rr.randomId, reactions.toVector)
      )
      _ ← dialogExt.ackRemoveReaction(peer, rr)
      _ ← dialogExt.ackRemoveReaction(peer, rr)
    } yield RemoveReactionAck(Some(seqState), reactions)) pipeTo sender()
  }

  protected def ackRemoveReaction(rr: RemoveReaction): Unit = {
    (for {
      reactions ← db.run(dialogExt.fetchReactions(peer, userId, rr.randomId))
      _ ← seqUpdExt.deliverUserUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, rr.randomId, reactions.toVector)
      )
    } yield RemoveReactionAck()) pipeTo sender()
  }

  /**
   * Yields unique message date in current dialog.
   * When `candidate` date is same as last message date, we increment `candidate` value by 1(ms),
   * thus resulting date can possibly be in future
   *
   * @return unique message date in current dialog
   */
  private def calcSendDate(): Long = {
    val candidate = state.nextDate // don't we do it twice?
    if (state.lastMessageDate == candidate) state.lastMessageDate + 1
    else candidate
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
   * @param mr message received request from client
   * @return `true` if we must process message received request and `false` otherwise
   */
  private def mustMakeReceive(mr: MessageReceived): Boolean =
    (mr.date > state.lastOwnerReceiveDate) &&
      (mr.date <= mr.now || mr.date <= state.lastMessageDate)

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
   * @param mr message received request from client
   * @return `true` if we must process message received request and `false` otherwise
   */
  private def mustMakeRead(mr: MessageRead): Boolean =
    (mr.date > state.lastOwnerReadDate) &&
      (mr.date <= mr.now || mr.date <= state.lastMessageDate)

  /**
   * check access hash and execute `f`, if access hash is valid
   * If `optAccessHash` is `None` - we simply don't check access hash
   * If `optSenderAuthId` is None, and we are validating access hash for private peer - it is invalid
   */
  private def withValidAccessHash[A](peer: Peer, optSenderAuthId: Option[Long], optAccessHash: Option[Long])(f: ⇒ Future[A]): Unit = {
    val validateHash = optAccessHash map { hash ⇒
      peer.`type` match {
        case PeerType.Private ⇒
          optSenderAuthId map { authId ⇒ userExt.checkAccessHash(peer.id, authId, hash) } getOrElse FastFuture.successful(false)
        case PeerType.Group ⇒
          groupExt.checkAccessHash(peer.id, hash)
        case unknown ⇒ throw new RuntimeException(s"Unknown peer type $unknown")
      }
    } getOrElse FastFuture.successful(true)

    (for {
      isValid ← validateHash
      result ← if (isValid) f else FastFuture.successful(Status.Failure(InvalidAccessHash))
    } yield result) pipeTo self
    ()
  }

}
