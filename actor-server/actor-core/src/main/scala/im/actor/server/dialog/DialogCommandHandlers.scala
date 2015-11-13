package im.actor.server.dialog

import akka.actor.{ ActorRef, Status }
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model.{ HistoryMessage, PeerType }
import im.actor.server.persist.{ DialogRepo, HistoryMessageRepo }
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

  protected def sendMessage(state: DialogState, sm: SendMessage): Unit = {
    (withCachedFuture[AuthSidRandomId, SeqStateDate](sm.senderAuthSid → sm.randomId) {
      for {
        SeqState(seq, state) ← deliveryExt.senderDelivery(userId, sm.senderAuthSid, peer, sm.randomId, sm.date, sm.message, sm.isFat)
        message = sm.message
        _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sm.date), sm.randomId, message.header, message.toByteArray))
        _ ← dialogExt.ackSendMessage(peer, sm)
      } yield SeqStateDate(seq, state, sm.date)
    } recover {
      case e ⇒
        log.error(e, "Failed to send message")
        throw e
    }) pipeTo sender() onSuccess {
      case _ ⇒
        if (state.isHidden)
          self.tell(Show(peer), ActorRef.noSender)
    }
    updateMessageDate(state, sm.date)
  }

  protected def ackSendMessage(state: DialogState, sm: SendMessage): Unit = {
    if (peer.typ == PeerType.Private) SocialManager.recordRelation(sm.origin.id, userId)

    deliveryExt
      .receiverDelivery(userId, sm.origin.id, peer, sm.randomId, sm.date, sm.message, sm.isFat)
      .map(_ ⇒ SendMessageAck())
      .pipeTo(sender()) onSuccess {
        case _ ⇒
          if (state.isHidden)
            self.tell(Show(peer), ActorRef.noSender)
      }
  }

  protected def writeMessage(
    dateMillis: Long,
    randomId:   Long,
    message:    ApiMessage
  ): Unit = {
    val date = new DateTime(dateMillis)
    val fut =
      message match {
        case ApiServiceMessage(_, Some(ApiServiceExContactRegistered(_))) ⇒
          db.run(HistoryMessageRepo.create(
            HistoryMessage(
              userId = peer.id, //figure out what should be there
              peer = selfPeer,
              date = date,
              senderUserId = userId,
              randomId = randomId,
              messageContentHeader = message.header,
              messageContentData = message.toByteArray,
              deletedAt = None
            )
          ))
        case _ ⇒
          db.run(writeHistoryMessage(
            selfPeer,
            peer,
            date,
            randomId,
            message.header,
            message.toByteArray
          ))
      }
    fut map (_ ⇒ WriteMessageAck()) pipeTo sender()
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

  protected def ackMessageReceived(state: DialogState, mr: MessageReceived): Unit = {
    (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender() andThen {
      case Failure(e) ⇒ log.error(e, "Failed to ack MessageReceived")
    }
  }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr)

    val readerUpd = for {
      _ ← db.run(markMessagesRead(selfPeer, peer, new DateTime(mr.date)))
      _ ← deliveryExt.read(userId, mr.readerAuthSid, peer, mr.date)
    } yield ()

    (if (mustRead) {
      for {
        _ ← readerUpd
        _ ← dialogExt.ackMessageRead(peer, mr)
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

  private def mustMakeReceive(state: DialogState, mr: MessageReceived): Boolean =
    (mr.date > state.lastReceiveDate) && //receive date is later than last receive date
      (mr.date <= mr.now) && // and receive date is not in future
      (state.lastMessageDate == 0L || mr.date > state.lastMessageDate) //and receive date if after date of last message sent by this user

  private def mustMakeRead(state: DialogState, mr: MessageRead): Boolean =
    (mr.date > state.lastReadDate) && //read date is later than last read date
      (mr.date <= mr.now) && // and read date is not in future
      (state.lastMessageDate == 0L || mr.date > state.lastMessageDate) //and read date if after date of last message sent by this user

  protected def updateMessageDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastMessageDate(date)))

  private def updateReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReceiveDate(date)))

  private def updateReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastReadDate(date)))

  private def updateShown(state: DialogState): Unit =
    context become initialized(state.updated(Shown))

  private def updateHidden(state: DialogState): Unit =
    context become initialized(state.updated(Hidden))

}
