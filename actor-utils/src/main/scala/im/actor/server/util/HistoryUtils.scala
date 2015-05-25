package im.actor.server.util

import scala.concurrent.ExecutionContext

import org.joda.time.DateTime
import slick.dbio.Effect.{ Write, Read }
import slick.dbio.{ NoStream, DBIOAction, DBIO }

import im.actor.server.{ models, persist }

object HistoryUtils {
  def writeHistoryMessage(
    fromPeer:             models.Peer,
    toPeer:               models.Peer,
    date:                 DateTime,
    randomId:             Long,
    messageContentHeader: Int,
    messageContentData:   Array[Byte]
  )(implicit ec: ExecutionContext): DBIOAction[Any, NoStream, Read with Write] = {
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
      } yield res
    } else if (toPeer.typ == models.PeerType.Group) {
      persist.GroupUser.findUserIds(toPeer.id) flatMap { groupUserIds ⇒

        // TODO: #perf eliminate double loop

        val historyMessages = groupUserIds.map { groupUserId ⇒
          models.HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
        }

        // TODO: #perf update dialogs in one query
        val dialogActions = groupUserIds.map(persist.Dialog.updateLastMessageDate(_, toPeer, date))

        DBIO.sequence(dialogActions :+ (persist.HistoryMessage.create(historyMessages) map (_.getOrElse(0)))) map (_.foldLeft(0)(_ + _))
      }
    } else {
      throw new NotImplementedError()
    }
  }

  def markMessagesReceived(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext) = {
    requirePrivatePeer(byPeer)
    requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.Dialog.updateLastReceivedAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.Dialog.updateOwnerLastReceivedAt(byPeer.id, peer, date)
        ))
      case models.PeerType.Group ⇒
        persist.GroupUser.findUserIds(peer.id) flatMap { groupUserIds ⇒
          // TODO: #perf update dialogs in one query

          val actions = groupUserIds.view.filterNot(_ == byPeer.id) map { groupUserId ⇒
            persist.Dialog.updateOwnerLastReceivedAt(groupUserId, models.Peer.group(peer.id), date)
          }

          DBIO.sequence(actions)
        }
    }
  }

  def markMessagesRead(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext) = {
    requirePrivatePeer(byPeer)
    requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.Dialog.updateLastReadAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.Dialog.updateOwnerLastReadAt(byPeer.id, peer, date)
        ))
      case models.PeerType.Group ⇒
        persist.GroupUser.findUserIds(peer.id) flatMap { groupUserIds ⇒
          // TODO: #perf update dialogs in one query

          val selfAction = persist.Dialog.updateOwnerLastReadAt(byPeer.id, models.Peer.group(peer.id), date)

          val actions = groupUserIds.view.filterNot(_ == byPeer.id) map { groupUserId ⇒
            persist.Dialog.updateLastReadAt(groupUserId, models.Peer.group(peer.id), date)
          }

          DBIO.sequence(actions :+ selfAction)
        }
    }
  }

  private def requireDifferentPeers(peer1: models.Peer, peer2: models.Peer) = {
    if (peer1 == peer2)
      throw new Exception("peers should not be same")
  }

  private def requirePrivatePeer(peer: models.Peer) = {
    if (peer.typ != models.PeerType.Private)
      throw new Exception("peer should be Private")
  }
}
