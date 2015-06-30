package im.actor.server.api.rpc.service.messaging

import scala.concurrent.Future

import org.joda.time.DateTime
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ OutPeer, PeerType }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.util.{ GroupUtils, UserUtils }
import im.actor.server.{ models, persist }

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import GroupUtils._
  import UserUtils._
  import im.actor.api.rpc.Implicits._
  import im.actor.server.push.SeqUpdatesManager._

  override def jhandleMessageReceived(peer: OutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(peer) {
        val receivedDate = System.currentTimeMillis()

        peer.`type` match {
          case PeerType.Private ⇒
            PrivatePeerManager.messageReceived(peer.id, client.userId, client.authId, date, receivedDate)

            DBIO.successful(Ok(ResponseVoid))
          case PeerType.Group ⇒
            GroupPeerManager.messageReceived(peer.id, client.userId, client.authId, date, receivedDate)

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
            PrivatePeerManager.messageRead(peer.id, client.userId, client.authId, date, readDate)

            DBIO.successful(Ok(ResponseVoid))
          case PeerType.Group ⇒
            GroupPeerManager.messageRead(peer.id, client.userId, client.authId, date, readDate)

            DBIO.successful(Ok(ResponseVoid))
          case _ ⇒ throw new Exception("Not implemented")
        }
      }
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleClearChat(peer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val update = UpdateChatClear(peer.asPeer)

      for {
        _ ← persist.HistoryMessage.deleteAll(client.userId, peer.asModel)
        seqstate ← broadcastClientUpdate(update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(action map (_.transactionally)))
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

  override def jhandleLoadDialogs(startDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadDialogs]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      persist.Dialog.findByUser(client.userId, endDateTimeFrom(startDate), limit) flatMap { dialogModels ⇒
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

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleLoadHistory(peer: OutPeer, endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadHistory]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      persist.Dialog.find(client.userId, peer.asModel).headOption.flatMap {
        case Some(dialog) ⇒
          persist.HistoryMessage.find(client.userId, peer.asModel, endDateTimeFrom(endDate), limit) flatMap { messageModels ⇒
            val lastReceivedAt = dialog.lastReceivedAt
            val lastReadAt = dialog.lastReadAt

            val (messages, userIds) = messageModels.foldLeft(Vector.empty[HistoryMessage], Set.empty[Int]) {
              case ((msgs, userIds), message) ⇒
                val newMsgs = msgs :+ message.asStruct(lastReceivedAt, lastReadAt)

                val newUserIds = if (message.senderUserId != client.userId)
                  userIds + message.senderUserId
                else
                  userIds

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
        case None ⇒
          DBIO.successful(Ok(ResponseLoadHistory(Vector.empty, Vector.empty)))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleDeleteMessage(peer: OutPeer, randomIds: Vector[Long], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      val update = UpdateMessageDelete(peer.asPeer, randomIds)

      for {
        _ ← persist.HistoryMessage.delete(client.userId, peer.asModel, randomIds.toSet)
        seqstate ← broadcastClientUpdate(update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(action map (_.transactionally)))
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

  private def getDialogStruct(dialogModel: models.Dialog): DBIOAction[Dialog, NoStream, Read with Read] = {
    for {
      messageOpt ← persist.HistoryMessage.find(dialogModel.userId, dialogModel.peer).headOption
      unreadCount ← persist.HistoryMessage.getUnreadCount(dialogModel.userId, dialogModel.peer, dialogModel.ownerLastReadAt)
    } yield {
      val emptyMessageContent = TextMessage(text = "", mentions = Vector.empty, ext = None)
      val messageModel = messageOpt.getOrElse(models.HistoryMessage(dialogModel.userId, dialogModel.peer, new DateTime(0), 0, 0, emptyMessageContent.header, emptyMessageContent.toByteArray, None))
      val message = messageModel.asStruct(dialogModel.lastReceivedAt, dialogModel.lastReadAt)

      Dialog(
        peer = dialogModel.peer.asStruct,
        unreadCount = unreadCount,
        sortDate = message.date,
        senderUserId = message.senderUserId,
        randomId = message.randomId,
        date = message.date,
        message = message.message,
        state = message.state
      )
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
