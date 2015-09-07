package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.dialog.group.GroupDialogOperations
import im.actor.server.dialog.privat.PrivateDialogOperations
import im.actor.server.dialog.{ ReadFailed, ReceiveFailed }
import im.actor.server.group.{ GroupUtils, GroupOffice }
import im.actor.server.history.HistoryUtils
import im.actor.server.user.{ UserUtils, UserOffice }
import im.actor.server.{ models, persist }
import org.joda.time.DateTime
import slick.dbio
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

object HistoryErrors {
  val ReceiveFailed = RpcError(500, "RECEIVE_FAILED", "", true, None)
  val ReadFailed = RpcError(500, "READ_FAILED", "", true, None)

}

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import GroupUtils._
  import HistoryUtils._
  import UserUtils._
  import im.actor.api.rpc.Implicits._

  override def jhandleMessageReceived(peer: OutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val receivedFuture = peer.`type` match {
        case PeerType.Private ⇒
          for {
            _ ← PrivateDialogOperations.messageReceived(client.userId, peer.id, date)
          } yield Ok(ResponseVoid)
        case PeerType.Group ⇒
          for {
            _ ← GroupDialogOperations.messageReceived(peer.id, client.userId, client.authId, date)
          } yield Ok(ResponseVoid)
        case _ ⇒ throw new Exception("Not implemented")
      }
      DBIO.from(receivedFuture)
    }

    db.run(toDBIOAction(action)) recover {
      case ReceiveFailed ⇒ Error(HistoryErrors.ReceiveFailed)
    }
  }

  override def jhandleMessageRead(peer: OutPeer, date: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val readFuture = peer.`type` match {
        case PeerType.Private ⇒
          for {
            _ ← PrivateDialogOperations.messageRead(client.userId, client.authId, peer.id, date)
          } yield Ok(ResponseVoid)
        case PeerType.Group ⇒
          for {
            _ ← GroupDialogOperations.messageRead(peer.id, client.userId, client.authId, date)
          } yield Ok(ResponseVoid)
        case _ ⇒ throw new Exception("Not implemented")
      }
      DBIO.from(readFuture)
    }

    db.run(toDBIOAction(action)) recover {
      case ReadFailed ⇒ Error(HistoryErrors.ReadFailed)
    }
  }

  override def jhandleClearChat(peer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      val update = UpdateChatClear(peer.asPeer)

      for {
        _ ← fromDBIOBoolean(CommonErrors.forbidden("Clearing of public chats is forbidden")) {
          if (peer.`type` == PeerType.Private) {
            DBIO.successful(true)
          } else {
            DBIO.from(GroupOffice.isHistoryShared(peer.id)) flatMap (isHistoryShared ⇒ DBIO.successful(!isHistoryShared))
          }
        }
        _ ← fromDBIO(persist.HistoryMessage.deleteAll(client.userId, peer.asModel))
        seqstate ← fromFuture(UserOffice.broadcastClientUpdate(update, None, isFat = false))
      } yield ResponseSeq(seqstate.seq, seqstate.state.toByteArray)
    }

    db.run(toDBIOAction(action map (_.run)))
  }

  override def jhandleDeleteChat(peer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val update = UpdateChatDelete(peer.asPeer)

      for {
        _ ← persist.HistoryMessage.deleteAll(client.userId, peer.asModel)
        _ ← persist.Dialog.delete(client.userId, peer.asModel)
        seqstate ← DBIO.from(UserOffice.broadcastClientUpdate(update, None, isFat = false))
      } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleLoadDialogs(endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadDialogs]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      persist.Dialog.findByUser(client.userId, endDateTimeFrom(endDate), Int.MaxValue) flatMap { dialogModels ⇒
        for {
          dialogs ← DBIO.sequence(dialogModels map getDialogStruct)
          (users, groups) ← getDialogsUsersGroups(dialogs)
        } yield {
          Ok(ResponseLoadDialogs(
            groups = groups.toVector,
            users = users.toVector,
            dialogs = dialogs.toVector
          ))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleLoadHistory(peer: OutPeer, endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadHistory]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(peer) {
        withHistoryOwner(peer.asModel) { historyOwner ⇒
          persist.Dialog.find(client.userId, peer.asModel) flatMap { dialogOpt ⇒
            persist.HistoryMessage.find(historyOwner, peer.asModel, endDateTimeFrom(endDate), limit) flatMap { messageModels ⇒
              val lastReceivedAt = dialogOpt map (_.lastReceivedAt) getOrElse (new DateTime(0))
              val lastReadAt = dialogOpt map (_.lastReadAt) getOrElse (new DateTime(0))

              val (messages, userIds) = messageModels.view
                .map(_.ofUser(client.userId))
                .foldLeft(Vector.empty[HistoryMessage], Set.empty[Int]) {
                  case ((msgs, userIds), message) ⇒
                    val messageStruct = message.asStruct(lastReceivedAt, lastReadAt)
                    val newMsgs = msgs :+ messageStruct

                    val newUserIds = relatedUsers(messageStruct.message) ++
                      (if (message.senderUserId != client.userId)
                        userIds + message.senderUserId
                      else
                        userIds)

                    (newMsgs, newUserIds)
                }

              for {
                userStructs ← DBIO.from(Future.sequence(userIds.toVector map (UserOffice.getApiStruct(_, client.userId, client.authId))))
              } yield {
                Ok(ResponseLoadHistory(messages, userStructs))
              }
            }
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleDeleteMessage(outPeer: OutPeer, randomIds: Vector[Long], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(outPeer) {
        val peer = outPeer.asModel

        withHistoryOwner(peer) { historyOwner ⇒
          if (isSharedUser(historyOwner)) {
            persist.HistoryMessage.find(historyOwner, peer, randomIds.toSet) flatMap { messages ⇒
              if (messages.exists(_.senderUserId != client.userId)) {
                DBIO.successful(Error(CommonErrors.forbidden("You can only delete your own messages")))
              } else {
                val update = UpdateMessageDelete(outPeer.asPeer, randomIds)

                for {
                  _ ← persist.HistoryMessage.delete(historyOwner, peer, randomIds.toSet)
                  groupUserIds ← persist.GroupUser.findUserIds(peer.id) map (_.toSet)
                  (seqstate, _) ← DBIO.from(UserOffice.broadcastClientAndUsersUpdate(groupUserIds, update, None, false))
                } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              }
            }
          } else {
            val update = UpdateMessageDelete(outPeer.asPeer, randomIds)
            for {
              _ ← persist.HistoryMessage.delete(client.userId, peer, randomIds.toSet)
              seqstate ← DBIO.from(UserOffice.broadcastClientUpdate(update, None, isFat = false))
            } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
          }
        }
      }
    }

    db.run(toDBIOAction(action))
  }

  private val MaxDate = (new DateTime(294276, 1, 1, 0, 0)).getMillis

  private def endDateTimeFrom(date: Long): Option[DateTime] = {
    if (date == 0l) {
      None
    } else {
      Some(new DateTime(
        if (date >= MaxDate)
          new DateTime(294276, 1, 1, 0, 0)
        else
          date
      ))
    }
  }

  private def getDialogStruct(dialogModel: models.Dialog)(implicit client: AuthorizedClientData): dbio.DBIO[Dialog] = {
    withHistoryOwner(dialogModel.peer) { historyOwner ⇒
      for {
        messageOpt ← persist.HistoryMessage.findNewest(historyOwner, dialogModel.peer) map (_.map(_.ofUser(client.userId)))
        unreadCount ← getUnreadCount(historyOwner, dialogModel.peer, dialogModel.ownerLastReadAt)
      } yield {
        val emptyMessageContent = TextMessage(text = "", mentions = Vector.empty, ext = None)
        val messageModel = messageOpt.getOrElse(models.HistoryMessage(dialogModel.userId, dialogModel.peer, new DateTime(0), 0, 0, emptyMessageContent.header, emptyMessageContent.toByteArray, None))
        val message = messageModel.asStruct(dialogModel.lastReceivedAt, dialogModel.lastReadAt)

        Dialog(
          peer = dialogModel.peer.asStruct,
          unreadCount = unreadCount,
          sortDate = dialogModel.lastMessageDate.getMillis,
          senderUserId = message.senderUserId,
          randomId = message.randomId,
          date = message.date,
          message = message.message,
          state = message.state
        )
      }
    }
  }

  private def getUnreadCount(historyOwner: Int, peer: models.Peer, ownerLastReadAt: DateTime)(implicit client: AuthorizedClientData): DBIO[Int] = {
    if (isSharedUser(historyOwner)) {
      for {
        isMember ← DBIO.from(GroupOffice.getMemberIds(peer.id) map { case (memberIds, _, _) ⇒ memberIds contains client.userId })
        result ← if (isMember) persist.HistoryMessage.getUnreadCount(historyOwner, peer, ownerLastReadAt) else DBIO.successful(0)
      } yield result
    } else {
      persist.HistoryMessage.getUnreadCount(historyOwner, peer, ownerLastReadAt)
    }
  }

  private def getDialogsUsersGroups(dialogs: Seq[Dialog])(implicit client: AuthorizedClientData) = {
    val (userIds, groupIds) = dialogs.foldLeft((Set.empty[Int], Set.empty[Int])) {
      case ((uacc, gacc), dialog) ⇒
        if (dialog.peer.`type` == PeerType.Private) {
          (uacc ++ relatedUsers(dialog.message) ++ Set(dialog.peer.id, dialog.senderUserId), gacc)
        } else {
          (uacc ++ relatedUsers(dialog.message) + dialog.senderUserId, gacc + dialog.peer.id)
        }
    }

    for {
      groups ← DBIO.from(Future.sequence(groupIds map (GroupOffice.getApiStruct(_, client.userId))))
      groupUserIds = groups.map(g ⇒ g.members.map(m ⇒ Seq(m.userId, m.inviterUserId)).flatten :+ g.creatorUserId).flatten
      users ← DBIO.from(Future.sequence((userIds ++ groupUserIds).filterNot(_ == 0) map (UserOffice.getApiStruct(_, client.userId, client.authId))))
    } yield (users, groups)
  }

  private def relatedUsers(message: Message): Set[Int] = {
    message match {
      case ServiceMessage(_, extOpt)   ⇒ extOpt map (relatedUsers) getOrElse (Set.empty)
      case TextMessage(_, mentions, _) ⇒ mentions.toSet
      case JsonMessage(_)              ⇒ Set.empty
      case _: DocumentMessage          ⇒ Set.empty
    }
  }

  private def relatedUsers(ext: ServiceEx): Set[Int] =
    ext match {
      case ServiceExContactRegistered(userId)               ⇒ Set(userId)
      case ServiceExChangedAvatar(_)                        ⇒ Set.empty
      case ServiceExChangedTitle(_)                         ⇒ Set.empty
      case ServiceExGroupCreated | _: ServiceExGroupCreated ⇒ Set.empty
      case ServiceExPhoneCall(_)                            ⇒ Set.empty
      case ServiceExPhoneMissed | _: ServiceExPhoneMissed   ⇒ Set.empty
      case ServiceExUserInvited(invitedUserId)              ⇒ Set(invitedUserId)
      case ServiceExUserJoined | _: ServiceExUserJoined     ⇒ Set.empty
      case ServiceExUserKicked(kickedUserId)                ⇒ Set(kickedUserId)
      case ServiceExUserLeft | _: ServiceExUserLeft         ⇒ Set.empty
    }
}
