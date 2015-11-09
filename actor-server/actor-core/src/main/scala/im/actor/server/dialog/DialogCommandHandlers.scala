package im.actor.server.dialog

import akka.actor.{ ActorRef, Status }
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model.{ Peer, HistoryMessage, PeerType }
import im.actor.server.persist.{ DialogRepo, HistoryMessageRepo }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future

trait DialogCommandHandlers extends UpdateCounters with PeersImplicits {
  this: Dialog ⇒

  import DialogCommands._
  import DialogEvents._

  protected def sendMessage(state: DialogState, sm: SendMessage): Unit = {
    val sendFuture = (withCachedFuture[AuthSidRandomId, SeqStateDate](sm.senderAuthSid → sm.randomId) {
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
    }) pipeTo sender()
    sendFuture onSuccess {
      case SeqStateDate(_, _, date) ⇒ self ! LastOwnMessageDate(date)
    }
  }

  protected def ackSendMessage(state: DialogState, sm: SendMessage): Unit = {
    if (peer.typ == PeerType.Private) SocialManager.recordRelation(sm.origin.id, userId)

    deliveryExt
      .receiverDelivery(userId, sm.origin.id, peer, sm.randomId, sm.date, sm.message, sm.isFat)
      .map(_ ⇒ SendMessageAck())
      .pipeTo(sender())

    if (state.isHidden)
      self.tell(Show(peer), ActorRef.noSender)

    //    onSuccess(fu) { _ =>
    //      updatePeerMessageDate()
    //    }

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
    val receiveFuture = (if (mustReceive) {
      for {
        _ ← dialogExt.ackMessageReceived(peer, mr)
        _ ← db.run(markMessagesReceived(selfPeer, peer, new DateTime(mr.date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }) pipeTo sender()
    if (mustReceive) {
      onSuccess(receiveFuture) { _ ⇒
        updateOwnReceiveDate(state, mr.date)
      }
    }
  }

  protected def ackMessageReceived(state: DialogState, mr: MessageReceived): Unit = {
    val notifyFuture = (deliveryExt.notifyReceive(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReceivedAck() }) pipeTo sender()
    onSuccess(notifyFuture) { _ ⇒
      updatePeerReceiveDate(state, mr.date)
    }
  }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr)
    val readFuture = (if (mustRead) {
      for {
        _ ← db.run(markMessagesRead(selfPeer, peer, new DateTime(mr.date)))
        //maybe it should be before condition
        _ ← deliveryExt.read(userId, mr.readerAuthSid, peer, mr.date)
        _ ← dialogExt.ackMessageRead(peer, mr)
      } yield MessageReadAck()
    } else {
      Future.successful(MessageReadAck())
    }) pipeTo sender()
    if (mustRead) {
      onSuccess(readFuture) { _ ⇒
        updateOwnReadDate(state, mr.date)
      }
    }
  }

  protected def ackMessageRead(state: DialogState, mr: MessageRead): Unit = {
    val notifyFuture = (deliveryExt.notifyRead(userId, peer, mr.date, mr.now) map { _ ⇒ MessageReadAck() }) pipeTo sender()
    onSuccess(notifyFuture) { _ ⇒
      updatePeerReadDate(state, mr.date)
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
          _ ← db.run(DialogRepo.hide(userId, peer))
          seqstate ← userExt.notifyDialogsChanged(userId)
        } yield seqstate) pipeTo sender()

      onSuccess(future) { _ ⇒
        updateHidden(state)
      }
    }
  }

  private def mustMakeReceive(state: DialogState, mr: MessageReceived): Boolean = peer match {
    case Peer(PeerType.Private, _) ⇒
      (mr.date > state.lastOwnReceiveDate) && //receive date is later than last receive date
        (mr.date <= mr.now) && // and receive date is not in future
        (state.lastOwnMessageDate == 0L || mr.date > state.lastOwnMessageDate) //and receive date if after date of last message sent by this user
    case Peer(PeerType.Group, _) ⇒
      //maybe add part about ownReceivedDate
      !(state.lastPeerReceiveDate >= mr.date) //&& !state.lastSenderId.contains(receiverUserId)
  }

  private def mustMakeRead(state: DialogState, mr: MessageRead): Boolean = peer match {
    case Peer(PeerType.Private, _) ⇒
      (mr.date > state.lastOwnReadDate) && //read date is later than last read date
        (mr.date <= mr.now) && // and read date is not in future
        (state.lastOwnMessageDate == 0L || mr.date > state.lastOwnMessageDate) //and read date if after date of last message sent by this user
    case Peer(PeerType.Group, _) ⇒
      !(state.lastPeerReadDate >= mr.date) //&& !state.lastSenderId.contains(readerUserId)
  }

  protected def updateOwnMessageDate(state: DialogState, md: LastOwnMessageDate): Unit =
    context become initialized(state.updated(md))

  private def updatePeerReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastPeerReceiveDate(date)))

  private def updatePeerReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastPeerReadDate(date)))

  private def updateOwnReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastOwnReceiveDate(date)))

  private def updateOwnReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastOwnReadDate(date)))

  private def updateShown(state: DialogState): Unit =
    context become initialized(state.updated(Shown))

  private def updateHidden(state: DialogState): Unit =
    context become initialized(state.updated(Hidden))

}
