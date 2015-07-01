package im.actor.server.util

import scala.concurrent.ExecutionContext
import scala.util.control.NoStackTrace

import org.joda.time.DateTime
import slick.dbio.DBIO

import im.actor.api.rpc.AuthorizedClientData
import im.actor.server.{ models, persist }

object HistoryUtils {

  import GroupUtils._

  // User for writing history in public groups
  private val sharedUserId = 0

  def writeHistoryMessage(
    fromPeer:             models.Peer,
    toPeer:               models.Peer,
    date:                 DateTime,
    randomId:             Long,
    messageContentHeader: Int,
    messageContentData:   Array[Byte]
  )(
    implicit
    ec: ExecutionContext
  ): DBIO[Unit] = {
    requirePrivatePeer(fromPeer)
    requireDifferentPeers(fromPeer, toPeer)

    if (toPeer.typ == models.PeerType.Private) {
      val outMessage = models.HistoryMessage(
        userId = fromPeer.id,
        peer = toPeer,
        date = date,
        senderUserId = fromPeer.id,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = None
      )

      val inMessage = models.HistoryMessage(
        userId = toPeer.id,
        peer = fromPeer,
        date = date,
        senderUserId = fromPeer.id,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = None
      )

      for {
        _ ← persist.HistoryMessage.create(Seq(outMessage, inMessage))
        _ ← persist.Dialog.updateLastMessageDate(fromPeer.id, toPeer, date)
        res ← persist.Dialog.updateLastMessageDate(toPeer.id, fromPeer, date)
      } yield ()
    } else if (toPeer.typ == models.PeerType.Group) {
      withGroup(toPeer.id) { group ⇒
        withGroupUserIds(group.id) { groupUserIds ⇒
          if (group.isPublic) {
            val historyMessage = models.HistoryMessage(sharedUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)

            for {
              _ ← persist.Dialog.updateLastMessageDates(groupUserIds.toSet, toPeer, date)
              _ ← persist.HistoryMessage.create(historyMessage)
            } yield ()
          } else {
            val historyMessages = groupUserIds.map { groupUserId ⇒
              models.HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
            }
            val dialogAction = persist.Dialog.updateLastMessageDates(groupUserIds.toSet, toPeer, date)

            DBIO.sequence(Seq(dialogAction, (persist.HistoryMessage.create(historyMessages) map (_.getOrElse(0))))) map (_ ⇒ ())
          }
        }
      }
    } else {
      DBIO.failed(new Exception("PeerType is not supported") with NoStackTrace)
    }
  }

  def markMessagesReceived(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.Dialog.updateLastReceivedAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.Dialog.updateOwnerLastReceivedAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case models.PeerType.Group ⇒
        withGroup(peer.id) { group ⇒
          persist.GroupUser.findUserIds(peer.id) flatMap { groupUserIds ⇒
            // TODO: #perf update dialogs in one query

            val actions = groupUserIds.view.filterNot(_ == byPeer.id) map { groupUserId ⇒
              persist.Dialog.updateOwnerLastReceivedAt(groupUserId, models.Peer.group(peer.id), date)
            }

            DBIO.sequence(actions) map (_ ⇒ ())
          }
        }
    }
  }

  def markMessagesRead(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.Dialog.updateLastReadAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.Dialog.updateOwnerLastReadAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case models.PeerType.Group ⇒
        withGroup(peer.id) { group ⇒
          persist.GroupUser.findUserIds(peer.id) flatMap { groupUserIds ⇒
            // TODO: #perf update dialogs in one query

            val selfAction = persist.Dialog.updateOwnerLastReadAt(byPeer.id, models.Peer.group(peer.id), date)

            val actions = groupUserIds.view.filterNot(_ == byPeer.id) map { groupUserId ⇒
              persist.Dialog.updateLastReadAt(groupUserId, models.Peer.group(peer.id), date)
            }

            DBIO.sequence(actions :+ selfAction) map (_ ⇒ ())
          }
        }
    }
  }

  def withHistoryOwner[A](peer: models.Peer)(f: Int ⇒ DBIO[A])(
    implicit
    ec:     ExecutionContext,
    client: AuthorizedClientData
  ): DBIO[A] = {
    (peer.typ match {
      case models.PeerType.Private ⇒ DBIO.successful(client.userId)
      case models.PeerType.Group ⇒
        withGroup(peer.id) { group ⇒
          if (group.isPublic) {
            DBIO.successful(sharedUserId)
          } else {
            DBIO.successful(client.userId)
          }
        }
    }) flatMap f
  }

  def isSharedUser(userId: Int): Boolean = userId == sharedUserId

  private def requireDifferentPeers(peer1: models.Peer, peer2: models.Peer) = {
    if (peer1 == peer2)
      throw new Exception("peers should not be same")
  }

  private def requirePrivatePeer(peer: models.Peer) = {
    if (peer.typ != models.PeerType.Private)
      throw new Exception("peer should be Private")
  }
}
