package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.model.{ HistoryMessage, PeerType, Peer }
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.{ GroupUserRepo, HistoryMessageRepo }
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.{ Future, ExecutionContext }
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
        _ ← DialogRepo.updateLastMessageDatePrivate(fromPeer.id, toPeer, date)
        _ ← DialogRepo.updateLastMessageDatePrivate(toPeer.id, fromPeer, date)
      } yield ()
    } else if (toPeer.typ == PeerType.Group) {
      val groupInfo = for {
        isShared ← GroupExtension(system).isHistoryShared(toPeer.id)
        (memberIds, _, _) ← GroupExtension(system).getMemberIds(toPeer.id)
      } yield (isShared, memberIds)

      for {
        (isHistoryShared, groupUserIds) ← DBIO.from(groupInfo)
        _ ← DialogRepo.updateLastMessageDateGroup(toPeer, date)
        _ ← if (isHistoryShared) {
          val historyMessage = HistoryMessage(SharedUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
          HistoryMessageRepo.create(historyMessage) map (_ ⇒ ())
        } else {
          val historyMessages = groupUserIds.map { groupUserId ⇒
            HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
          }
          HistoryMessageRepo.create(historyMessages) map (_ ⇒ ())
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
      _ ← DialogRepo.updateLastMessageDatePrivate(userId, toPeer, date)
    } yield ()
  }

  private[dialog] def markMessagesReceived(byPeer: Peer, peer: Peer, date: DateTime)(implicit system: ActorSystem, ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    // requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case PeerType.Private ⇒
        DBIO.sequence(Seq(
          DialogRepo.updateLastReceivedAtPrivate(peer.id, Peer(PeerType.Private, byPeer.id), date),
          DialogRepo.updateOwnerLastReceivedAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case PeerType.Group ⇒
        withGroup(peer.id) { _ ⇒
          GroupUserRepo.findUserIds(peer.id) flatMap { groupUserIds ⇒
            for {
              _ ← DialogRepo.updateOwnerLastReceivedAt(byPeer.id, Peer(PeerType.Group, peer.id), date)
              _ ← DialogRepo.updateLastReceivedAtGroup(Peer(PeerType.Group, peer.id), date)
            } yield ()
          }
        }
      case _ ⇒ throw new RuntimeException(s"Unknown peer type ${peer.typ}")
    }
  }

  private[dialog] def markMessagesRead(byPeer: Peer, peer: Peer, date: DateTime)(implicit system: ActorSystem, ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    // requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case PeerType.Private ⇒
        DBIO.sequence(Seq(
          DialogRepo.updateLastReadAtPrivate(peer.id, Peer(PeerType.Private, byPeer.id), date),
          DialogRepo.updateOwnerLastReadAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case PeerType.Group ⇒
        withGroup(peer.id) { _ ⇒
          GroupUserRepo.findUserIds(peer.id) flatMap { groupUserIds ⇒
            for {
              _ ← DialogRepo.updateOwnerLastReadAt(byPeer.id, Peer(PeerType.Group, peer.id), date)
              _ ← DialogRepo.updateLastReadAtGroup(Peer(PeerType.Group, peer.id), date)
            } yield ()
          }
        }
      case _ ⇒ throw new RuntimeException(s"Unknown peer type ${peer.typ}")
    }
  }

  // todo: remove this in favor of getHistoryOwner
  def withHistoryOwner[A](peer: Peer, clientUserId: Int)(f: Int ⇒ DBIO[A])(implicit system: ActorSystem): DBIO[A] = {
    import system.dispatcher
    (peer.typ match {
      case PeerType.Private ⇒ DBIO.successful(clientUserId)
      case PeerType.Group ⇒
        DBIO.from(GroupExtension(system).isHistoryShared(peer.id)) flatMap { isHistoryShared ⇒
          if (isHistoryShared) {
            DBIO.successful(SharedUserId)
          } else {
            DBIO.successful(clientUserId)
          }
        }
      case _ ⇒ throw new RuntimeException(s"Unknown peer type ${peer.typ}")
    }) flatMap f
  }

  def getHistoryOwner(peer: Peer, clientUserId: Int)(implicit system: ActorSystem): Future[Int] = {
    import system.dispatcher
    peer.typ match {
      case PeerType.Private ⇒ Future.successful(clientUserId)
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
      throw new Exception("peer should be Private")
  }
}
