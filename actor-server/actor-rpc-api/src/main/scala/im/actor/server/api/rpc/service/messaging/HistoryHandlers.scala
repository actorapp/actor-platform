package im.actor.server.api.rpc.service.messaging

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent.Future

import org.joda.time.DateTime
import slick.dbio
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.util.{ AnyRefLogSource, HistoryUtils, GroupUtils, UserUtils }
import im.actor.server.{ models, persist }

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import GroupUtils._
  import HistoryUtils._
  import UserUtils._
  import im.actor.api.rpc.Implicits._
  import im.actor.server.push.SeqUpdatesManager._

  override def jhandleMessageReceived(peer: OutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(peer) {
        val receivedDate = System.currentTimeMillis()

        peer.`type` match {
          case PeerType.Private ⇒
            UserOffice.messageReceived(peer.id, client.userId, client.authId, date, receivedDate)

            DBIO.successful(Ok(ResponseVoid))
          case PeerType.Group ⇒
            GroupOffice.messageReceived(peer.id, client.userId, client.authId, date, receivedDate)

            DBIO.successful(Ok(ResponseVoid))
          case _ ⇒ throw new Exception("Not implemented")
        }
      }
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleMessageRead(peer: OutPeer, date: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(peer) {
        val readDate = System.currentTimeMillis()

        peer.`type` match {
          case PeerType.Private ⇒
            UserOffice.messageRead(peer.id, client.userId, client.authId, date, readDate)

            DBIO.successful(Ok(ResponseVoid))
          case PeerType.Group ⇒
            GroupOffice.messageRead(peer.id, client.userId, client.authId, date, readDate)

            DBIO.successful(Ok(ResponseVoid))
          case _ ⇒ throw new Exception("Not implemented")
        }
      }
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleClearChat(peer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      val update = UpdateChatClear(peer.asPeer)

      for {
        _ ← fromDBIOBoolean(CommonErrors.forbidden("Clearing of public chats is forbidden")) {
          if (peer.`type` == PeerType.Private) {
            DBIO.successful(true)
          } else {
            withGroup(peer.id)(g ⇒ DBIO.successful(!g.isPublic))
          }
        }
        _ ← fromDBIO(persist.HistoryMessage.deleteAll(client.userId, peer.asModel))
        seqstate ← fromDBIO(broadcastClientUpdate(update, None))
      } yield ResponseSeq(seqstate._1, seqstate._2)
    }

    db.run(toDBIOAction(action map (_.run)))
  }

  override def jhandleDeleteChat(peer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val update = UpdateChatDelete(peer.asPeer)

      for {
        _ ← persist.HistoryMessage.deleteAll(client.userId, peer.asModel)
        _ ← persist.Dialog.delete(client.userId, peer.asModel)
        seqstate ← broadcastClientUpdate(update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(action map (_.transactionally)))
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

              // TODO: #perf eliminate loooots of sql queries
              for {
                userModels ← persist.User.findByIds(userIds)
                userStructs ← DBIO.sequence(userModels.toVector map (userStruct(_, client.userId, client.authId)))
              } yield {
                Ok(ResponseLoadHistory(messages, userStructs))
              }
            }
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
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
                  (seqstate, _) ← broadcastClientAndUsersUpdate(groupUserIds, update, None, false)
                } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
              }
            }
          } else {
            val update = UpdateMessageDelete(outPeer.asPeer, randomIds)
            for {
              _ ← persist.HistoryMessage.delete(client.userId, peer, randomIds.toSet)
              seqstate ← broadcastClientUpdate(update, None)
            } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
          }
        }
      }
    }

    db.run(toDBIOAction(action map (_.transactionally)))
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
        unreadCount ← persist.HistoryMessage.getUnreadCount(historyOwner, dialogModel.peer, dialogModel.ownerLastReadAt)
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
      groups ← getGroupsStructs(groupIds)
      groupUserIds = groups.map(g ⇒ g.members.map(m ⇒ Seq(m.userId, m.inviterUserId)).flatten :+ g.creatorUserId).flatten
      users ← getUserStructs(userIds ++ groupUserIds, client.userId, client.authId)
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
