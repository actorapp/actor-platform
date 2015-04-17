package im.actor.server.api.rpc.service.conversations

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.conversations.{ ConversationsService, HistoryMessage, ResponseLoadDialogs, ResponseLoadHistory }
import im.actor.api.rpc.peers.OutPeer
import im.actor.server.persist

class ConversationsServiceImpl(implicit db: Database, actorSystem: ActorSystem) extends ConversationsService {

  import im.actor.server.api.util.UserUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  object Errors {
    val DialogNotFound = RpcError(404, "DIALOG_NOT_FOUND", "Dialog not found.", false, None)
  }

  override def jhandleLoadDialogs(startDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadDialogs]] = {
    throw new NotImplementedError()
  }

  override def jhandleLoadHistory(peer: OutPeer, endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadHistory]] = {
    val authorizedAction = requireAuth(clientData).map { client =>
      persist.Dialog.find(client.userId, peer.asModel).headOption.flatMap {
        case Some(dialog) =>
          persist.HistoryMessage.find(client.userId, peer.asModel, endDateTimeFrom(endDate), limit) flatMap { messageModels =>
            val lastReceivedAt = dialog.lastReceivedAt
            val lastReadAt = dialog.lastReadAt

            val (messages, userIds) = messageModels.foldLeft(Vector.empty[HistoryMessage], Set.empty[Int]) {
              case ((msgs, userIds), message) =>
                val newMsgs = msgs :+ message.asStruct(lastReceivedAt, lastReadAt)

                val newUserIds = if (message.senderUserId != client.userId)
                  userIds + message.senderUserId
                else
                  userIds

                (newMsgs, newUserIds)
            }

            // TODO: #perf eliminate loooots of sql queries
            for {
              userModels <- persist.User.findByIds(userIds)
              userStructs <- DBIO.sequence(userModels.toVector map (userStruct(_, client.userId, client.authId)))
            } yield {
              Ok(ResponseLoadHistory(messages, userStructs))
            }
          }
        case None =>
          DBIO.successful(Error(Errors.DialogNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  private val MaxDate = (new DateTime(294276, 1, 1, 0, 0)).getMillis

  private def endDateTimeFrom(date: Long): Option[DateTime] = {
    if (date == 0l) {
      None
    } else {
      Some(new DateTime(
        if (date > MaxDate)
          new DateTime(294276, 1, 1, 0, 0)
        else
          date
      ))
    }
  }
}
