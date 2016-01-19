package im.actor.server.dialog

import akka.actor.{ PoisonPill, ActorRef, Status }
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model._
import im.actor.server.persist.messaging.ReactionEventRepo
import im.actor.server.persist.{ DialogRepo, HistoryMessageRepo }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager
import im.actor.util.cache.CacheHelpers._
import im.actor.server.ApiConversions._
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.Future
import scala.util.Failure

trait DialogCommandHandlers extends UpdateCounters with PeersImplicits {
  this: DialogProcessor ⇒

  import DialogCommands._
  import DialogEvents._

  protected def sendMessage(state: DialogState, sm: SendMessage): Unit = {
    val sendFuture = (withCachedFuture[AuthSidRandomId, SeqStateDate](sm.senderAuthSid → sm.randomId) {
      for {
        _ ← dialogExt.ackSendMessage(peer, sm)
        message = sm.message
        _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sm.date), sm.randomId, message.header, message.toByteArray))
        _ ← dialogExt.updateCounters(peer, userId)
        SeqState(seq, state) ← deliveryExt.senderDelivery(userId, sm.senderAuthSid, peer, sm.randomId, sm.date, message, sm.isFat)
      } yield SeqStateDate(seq, state, sm.date)
    } recover {
      case e ⇒
        log.error(e, "Failed to send message")
        throw e
    }) pipeTo sender()
    onSuccess(sendFuture) { result ⇒
      if (state.isHidden) { self.tell(Show(peer), ActorRef.noSender) }
      updateMessageDate(state, sm.date, checkOpen = true)
    }
  }

  protected def updateCountersChanged(): Unit = {
    deliveryExt.sendCountersUpdate(userId)
      .map(_ ⇒ SendMessageAck())
      .pipeTo(sender())
  }

  protected def ackSendMessage(state: DialogState, sm: SendMessage): Unit = {
    if (peer.typ == PeerType.Private) {
      SocialManager.recordRelation(sm.origin.id, userId)
      SocialManager.recordRelation(userId, sm.origin.id)
    }

    deliveryExt
      .receiverDelivery(userId, sm.origin.id, peer, sm.randomId, sm.date, sm.message, sm.isFat)
      .map(_ ⇒ SendMessageAck())
      .pipeTo(sender()) onSuccess {
        case _ ⇒
          if (state.isHidden) { self.tell(Show(peer), ActorRef.noSender) }
          updateOpen(state)
      }
  }

  protected def writeMessage(
    dateMillis: Long,
    randomId:   Long,
    message:    ApiMessage
  ): Unit = {
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
    senderUserId: Int,
    dateMillis:   Long,
    randomId:     Long,
    message:      ApiMessage
  ): Unit = {
    val date = new DateTime(dateMillis)

    val result =
      if (peer.`type` == PeerType.Private && peer.id != senderUserId && userId != senderUserId) {
        Future.failed(new RuntimeException(s"writeMessageSelf with senderUserId ${senderUserId} in dialog of user ${userId} with user ${peer.id}"))
      } else {
        db.run(writeHistoryMessageSelf(userId, peer, senderUserId, date, randomId, message.header, message.toByteArray))
      }

    result map (_ ⇒ WriteMessageSelfAck()) pipeTo sender()
  }

  protected def messageReceived(state: DialogState, mr: MessageReceived): Unit = {
    val mustReceive = mustMakeReceive(state, mr)
    (if (mustReceive) {
      for {
        _ ← if (state.isOpen) dialogExt.ackMessageReceived(peer, mr) else Future.successful(())
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

  protected def ackMessageReceived(state: DialogState, mr: MessageReceived): Unit = {
    (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to ack MessageReceived")
    }
  }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr) && state.isOpen

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

  protected def ackMessageRead(state: DialogState, mr: MessageRead): Unit = {
    (deliveryExt.notifyRead(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReadAck() }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to ack MessageRead")
    }
  }

  protected def show(state: DialogState): Unit = {
    if (!state.isHidden)
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

  protected def hide(state: DialogState): Unit = {
    if (state.isHidden)
      sender ! Status.Failure(DialogErrors.DialogAlreadyHidden(peer))
    else {
      val future =
        (for {
          _ ← db.run(for {
            _ ← DialogRepo.hide(userId, peer)
            _ ← markMessagesRead(selfPeer, peer, new DateTime)
          } yield ())
          seqstate ← userExt.notifyDialogsChanged(userId)
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateHidden(state)
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

  protected def ackSetReaction(state: DialogState, sr: SetReaction): Unit = {
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

  protected def ackRemoveReaction(state: DialogState, rr: RemoveReaction): Unit = {
    (for {
      reactions ← db.run(dialogExt.fetchReactions(peer, userId, rr.randomId))
      _ ← seqUpdExt.deliverSingleUpdate(
        userId,
        UpdateReactionsUpdate(peer.asStruct, rr.randomId, reactions.toVector)
      )
    } yield RemoveReactionAck()) pipeTo sender()
  }

  private def mustMakeReceive(state: DialogState, mr: MessageReceived): Boolean =
    (mr.date > state.lastReceiveDate) && //receive date is later than last receive date
      (mr.date <= mr.now) // and receive date is not in future

  private def mustMakeRead(state: DialogState, mr: MessageRead): Boolean =
    (mr.date > state.lastReadDate) && //read date is later than last read date
      (mr.date <= mr.now) // and read date is not in future

  // if checkOpen is true, open dialog if it's not open already
  protected def updateMessageDate(state: DialogState, date: Long, checkOpen: Boolean): Unit = {
    val newState =
      (if (checkOpen && !state.isOpen)
        state.updated(Open)
      else
        state).updated(LastMessageDate(date))

    context become initialized(newState)
  }

  protected def updateOpen(state: DialogState): Unit =
    if (!state.isOpen) { context become initialized(state.updated(Open)) }

  private def updateReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReceiveDate(date)))

  private def updateReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReadDate(date)))

  private def updateShown(state: DialogState): Unit =
    context become initialized(state.updated(Shown))

  private def updateHidden(state: DialogState): Unit =
    context become initialized(state.updated(Hidden))

  private def updateFavourited(state: DialogState): Unit =
    context become initialized(state.updated(Favourited))

  private def updateUnfavourited(state: DialogState): Unit =
    context become initialized(state.updated(Unfavourited))
}
