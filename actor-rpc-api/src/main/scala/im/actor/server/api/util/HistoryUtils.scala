package im.actor.server.api.util

import scala.concurrent.ExecutionContext

import org.joda.time.DateTime
import slick.dbio.DBIO

import im.actor.server.{ models, persist }

object HistoryUtils {
  def writeHistoryMessage(fromPeer: models.Peer,
                          toPeer: models.Peer,
                          date: DateTime,
                          randomId: Long,
                          messageContentHeader: Int,
                          messageContentData: Array[Byte])(implicit ec: ExecutionContext) = {
    if (fromPeer.typ == models.PeerType.Group) {
      throw new Exception("fromPeer should be Private")
    } else {
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
          _ <- persist.HistoryMessage.create(Seq(outMessage, inMessage))
          _ <- persist.Dialog.updateLastMessageDate(fromPeer.id, toPeer, date)
          res <- persist.Dialog.updateLastMessageDate(toPeer.id, fromPeer, date)
        } yield res
      } else {
        persist.GroupUser.findUserIds(toPeer.id) flatMap { groupUserIds =>

          // TODO: #perf eliminate double loop

          val historyMessages = groupUserIds.map { groupUserId =>
            models.HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
          }

          // TODO: #perf update dialogs in one query
          val dialogActions = groupUserIds.map(persist.Dialog.updateLastMessageDate(_, toPeer, date))

          DBIO.sequence(dialogActions :+ persist.HistoryMessage.create(historyMessages))
        }
      }
    }
  }
}
