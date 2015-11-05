package im.actor.server.dialog

import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging._
import im.actor.server.dialog.HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model.{ Peer, HistoryMessage, PeerType }
import im.actor.server.persist.HistoryMessageRepo
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
        _ ← dialogExt.ackSendMessage(selfPeer, sm)
        message = sm.message
        _ ← db.run(writeHistoryMessage(selfPeer, peer, new DateTime(sm.date), sm.randomId, message.header, message.toByteArray))
      } yield SeqStateDate(seq, state, sm.date)
    } recover {
      case e ⇒
        log.error(e, "Failed to send message")
        throw e
    }) pipeTo sender()
    onSuccess(sendFuture) { result ⇒
      updateOwnMessageDate(state, result.date)
    }
  }

  protected def ackSendMessage(sm: SendMessage): Unit = {
    if (peer.typ == PeerType.Private) SocialManager.recordRelation(sm.origin.id, userId)
    val fu = deliveryExt.receiverDelivery(userId, sm.origin.id, peer, sm.randomId, sm.date, sm.message, sm.isFat) map (_ ⇒ SendMessageAck()) pipeTo sender()

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
    val receiveFuture = if (mustReceive) {
      for {
        _ ← dialogExt.ackMessageReceived(peer, mr)
        _ ← db.run(markMessagesReceived(selfPeer, peer, new DateTime(mr.date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }
    receiveFuture pipeTo sender()
    if (mustReceive) {
      onSuccess(receiveFuture) { _ ⇒
        updateOwnReceiveDate(state, mr.date)
      }
    }
  }

  protected def ackMessageReceived(state: DialogState, mr: MessageReceived): Unit =
    onSuccess(deliveryExt.notifyReceive(userId, peer, mr.date, mr.now)) { _ ⇒
      updatePeerReceiveDate(state, mr.date)
    }

  protected def messageRead(state: DialogState, mr: MessageRead): Unit = {
    val mustRead = mustMakeRead(state, mr)
    val readFuture = if (mustRead) {
      for {
        //maybe it should be before condition
        _ ← deliveryExt.read(userId, mr.readerAuthSid, peer, mr.date)
        _ ← dialogExt.ackMessageRead(peer, mr)
        _ ← db.run(markMessagesRead(selfPeer, peer, new DateTime(mr.date)))
      } yield MessageReadAck()
    } else {
      Future.successful(MessageReadAck())
    }
    readFuture pipeTo sender()
    if (mustRead) {
      onSuccess(readFuture) { _ ⇒
        updateOwnReadDate(state, mr.date)
      }
    }
  }

  protected def ackMessageRead(state: DialogState, mr: MessageRead): Unit =
    onSuccess(deliveryExt.notifyRead(userId, peer, mr.date, mr.now)) { _ ⇒
      updatePeerReadDate(state, mr.date)
    }

  private def mustMakeReceive(state: DialogState, mr: MessageReceived): Boolean = peer match {
    case Peer(PeerType.Private, _) ⇒
      !(state.lastOwnReceiveDate >= mr.date) &&
        !(mr.date > mr.now) &&
        (state.lastOwnMessageDate == 0L || state.lastOwnMessageDate >= mr.date)
    case Peer(PeerType.Group, _) ⇒
      //maybe add part about ownReceivedDate
      !(state.lastPeerReceiveDate >= mr.date) //&& !state.lastSenderId.contains(receiverUserId)
  }

  private def mustMakeRead(state: DialogState, mr: MessageRead): Boolean = peer match {
    case Peer(PeerType.Private, _) ⇒
      !(state.lastOwnReadDate >= mr.date) &&
        !(mr.date > mr.now) &&
        (state.lastOwnMessageDate == 0L || state.lastOwnMessageDate >= mr.date)
    case Peer(PeerType.Group, _) ⇒
      !(state.lastPeerReadDate >= mr.date) //&& !state.lastSenderId.contains(readerUserId)
  }

  private def updatePeerReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastPeerReceiveDate(date)))

  private def updatePeerReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastPeerReadDate(date)))

  private def updateOwnMessageDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastOwnMessageDate(date)))

  private def updateOwnReceiveDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastOwnReceiveDate(date)))

  private def updateOwnReadDate(state: DialogState, date: Long): Unit =
    context become initialized(state.updated(LastOwnReadDate(date)))

}
