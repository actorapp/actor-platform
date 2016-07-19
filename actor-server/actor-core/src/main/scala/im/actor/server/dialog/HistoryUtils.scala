package im.actor.server.dialog

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.model.{ HistoryMessage, Peer, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace

object HistoryUtils {

  import GroupUtils._

  // User for writing history in public groups
  val SharedUserId = 0

  private[dialog] def writeHistoryMessage(
    fromPeer:             Peer,
    toPeer:               Peer,
    date:                 DateTime,
    randomId:             Long,
    messageContentHeader: Int,
    messageContentData:   Array[Byte]
  )(implicit system: ActorSystem): DBIO[Unit] = {
    import system.dispatcher
    requirePrivatePeer(fromPeer)
    // requireDifferentPeers(fromPeer, toPeer)

    if (toPeer.typ == PeerType.Private) {
      val outMessage = HistoryMessage(
        userId = fromPeer.id,
        peer = toPeer,
        date = date,
        senderUserId = fromPeer.id,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = None
      )

      val messages =
        if (fromPeer != toPeer) {
          Seq(
            outMessage,
            outMessage.copy(userId = toPeer.id, peer = fromPeer)
          )
        } else {
          Seq(outMessage)
        }

      for {
        _ ← HistoryMessageRepo.create(messages)
      } yield ()
    } else if (toPeer.typ == PeerType.Group) {
      for {
        isHistoryShared ← DBIO.from(GroupExtension(system).isHistoryShared(toPeer.id))
        _ ← if (isHistoryShared) {
          val historyMessage = HistoryMessage(SharedUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
          HistoryMessageRepo.create(historyMessage) map (_ ⇒ ())
        } else {
          DBIO.from(GroupExtension(system).getMemberIds(toPeer.id)) map (_._1) flatMap { groupUserIds ⇒
            val historyMessages = groupUserIds.map { groupUserId ⇒
              HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
            }
            HistoryMessageRepo.create(historyMessages) map (_ ⇒ ())
          }
        }
      } yield ()
    } else {
      DBIO.failed(new Exception("PeerType is not supported") with NoStackTrace)
    }
  }

  private[dialog] def writeHistoryMessageSelf(
    userId:               Int,
    toPeer:               Peer,
    senderUserId:         Int,
    date:                 DateTime,
    randomId:             Long,
    messageContentHeader: Int,
    messageContentData:   Array[Byte]
  )(implicit ec: ExecutionContext): DBIO[Unit] = {
    for {
      _ ← HistoryMessageRepo.create(HistoryMessage(
        userId = userId,
        peer = toPeer,
        date = date,
        senderUserId = senderUserId,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = None
      ))
    } yield ()
  }

  def getHistoryOwner(peer: Peer, clientUserId: Int)(implicit system: ActorSystem): Future[Int] = {
    import system.dispatcher
    peer.typ match {
      case PeerType.Private ⇒ FastFuture.successful(clientUserId)
      case PeerType.Group ⇒
        for {
          isHistoryShared ← GroupExtension(system).isHistoryShared(peer.id)
        } yield if (isHistoryShared) SharedUserId else clientUserId
      case _ ⇒ throw new RuntimeException(s"Unknown peer type ${peer.typ}")
    }
  }

  def isSharedUser(userId: Int): Boolean = userId == SharedUserId

  private def requirePrivatePeer(peer: Peer) = {
    if (peer.typ != PeerType.Private)
      throw new RuntimeException("sender should be Private peer")
  }
}
