package im.actor.server.api.rpc.service.conversations

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import org.joda.time.DateTime
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.conversations._
import im.actor.api.rpc.groups.Group
import im.actor.api.rpc.messaging.{ MessageContent, TextMessage }
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.api.rpc.users.User
import im.actor.server.{ models, persist }

class ConversationsServiceImpl(implicit db: Database, actorSystem: ActorSystem) extends ConversationsService {

  import im.actor.server.api.util.GroupUtils._
  import im.actor.server.api.util.UserUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  object Errors {
    val DialogNotFound = RpcError(404, "DIALOG_NOT_FOUND", "Dialog not found.", false, None)
  }

  override def jhandleLoadDialogs(startDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadDialogs]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      persist.Dialog.findByUser(client.userId) flatMap { dialogModels =>
        for {
          dialogs <- DBIO.sequence(dialogModels map getDialogStruct)
          (users, groups) <- getDialogsUsersGroups(dialogs)
        } yield {
          Ok(ResponseLoadDialogs(
            groups = groups.toVector,
            users = users.toVector,
            dialogs = dialogs.toVector
          ))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
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

  private def getDialogStruct(dialogModel: models.Dialog): dbio.DBIOAction[Dialog, NoStream, Read with Read] = {
    for {
      messageOpt <- persist.HistoryMessage.find(dialogModel.userId, dialogModel.peer).headOption
      unreadCount <- persist.HistoryMessage.getUnreadCount(dialogModel.userId, dialogModel.peer, dialogModel.lastReadAt)
    } yield {
      val emptyMessageContent = TextMessage("", 0, None)
      val message = messageOpt.getOrElse(models.HistoryMessage(dialogModel.userId, dialogModel.peer, new DateTime(0), 0, 0, emptyMessageContent.header, emptyMessageContent.toByteArray, None))

      Dialog(
        peer = dialogModel.peer.asStruct,
        unreadCount = unreadCount,
        sortDate = message.date.getMillis,
        senderUserId = message.senderUserId,
        randomId = message.randomId,
        date = message.date.getMillis,
        message = MessageContent(emptyMessageContent.header, emptyMessageContent),
        state = None
      )
    }
  }

  private def getDialogsUsersGroups(dialogs: Seq[Dialog])
                                   (implicit client: AuthorizedClientData) = {
    val (userIds, groupIds) = dialogs.foldLeft((Set.empty[Int], Set.empty[Int])) {
      case ((uacc, gacc), dialog) =>
        if (dialog.peer.`type` == PeerType.Private) {
          (uacc + dialog.peer.id, gacc)
        } else {
          (uacc, gacc + dialog.peer.id)
        }
    }

    for {
      users <- userStructs(userIds, client.userId, client.authId)
      groups <- getGroupsStructs(groupIds)
    } yield (users, groups)
  }
}
