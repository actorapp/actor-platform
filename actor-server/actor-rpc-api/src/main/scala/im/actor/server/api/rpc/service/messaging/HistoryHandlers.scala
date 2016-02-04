package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.server.dialog.{ DialogErrors, HistoryUtils }
import im.actor.server.group.GroupUtils
import im.actor.server.model.{ HistoryMessage, Dialog, PeerType, Peer }
import im.actor.server.persist.{ GroupUserRepo, HistoryMessageRepo }
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserUtils
import org.joda.time.DateTime
import slick.dbio
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.language.postfixOps

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import HistoryUtils._
  import im.actor.api.rpc.Implicits._

  override def jhandleMessageReceived(peer: ApiOutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      dialogExt.messageReceived(peer.asPeer, client.userId, date) map (_ ⇒ Ok(ResponseVoid))
    }
  }

  override def jhandleMessageRead(peer: ApiOutPeer, date: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      dialogExt.messageRead(peer.asPeer, client.userId, client.authSid, date) map (_ ⇒ Ok(ResponseVoid))
    }
  }

  override def jhandleClearChat(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      val update = UpdateChatClear(peer.asPeer)

      for {
        _ ← fromDBIOBoolean(CommonErrors.forbidden("Clearing of public chats is forbidden")) {
          if (peer.`type` == ApiPeerType.Private) {
            DBIO.successful(true)
          } else {
            DBIO.from(groupExt.isHistoryShared(peer.id)) flatMap (isHistoryShared ⇒ DBIO.successful(!isHistoryShared))
          }
        }
        _ ← fromDBIO(HistoryMessageRepo.deleteAll(client.userId, peer.asModel))
        seqstate ← fromFuture(userExt.broadcastClientUpdate(update, None, isFat = false))
      } yield ResponseSeq(seqstate.seq, seqstate.state.toByteArray)
    }

    db.run(toDBIOAction(action map (_.run)))
  }

  override def jhandleDeleteChat(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← dialogExt.delete(client.userId, peer.asModel)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }
  }

  override def jhandleLoadDialogs(endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadDialogs]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      DialogRepo
        .findNotArchived(client.userId, endDateTimeFrom(endDate), limit, fetchHidden = true)
        .map(_ filterNot (dialogExt.dialogWithSelf(client.userId, _)))
        .flatMap { dialogModels ⇒
          for {
            dialogs ← DBIO.sequence(dialogModels map getDialogStruct) map (_.flatten)
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

  override def jhandleLoadGroupedDialogs(clientData: ClientData): Future[HandlerResult[ResponseLoadGroupedDialogs]] =
    authorized(clientData) { implicit client ⇒
      for {
        dialogGroups ← dialogExt.getGroupedDialogs(client.userId)
        (userIds, groupIds) = dialogGroups.view.flatMap(_.dialogs).foldLeft((Seq.empty[Int], Seq.empty[Int])) {
          case ((uids, gids), dialog) ⇒
            dialog.peer.`type` match {
              case ApiPeerType.Group   ⇒ (uids, gids :+ dialog.peer.id)
              case ApiPeerType.Private ⇒ (uids :+ dialog.peer.id, gids)
            }
        }
        (groups, users) ← GroupUtils.getGroupsUsers(groupIds, userIds, client.userId, client.authId)
      } yield Ok(ResponseLoadGroupedDialogs(dialogGroups, users.toVector, groups.toVector))
    }

  override def jhandleHideDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { implicit client ⇒
      (for {
        seqstate ← dialogExt.hide(client.userId, peer.asModel)
        groups ← dialogExt.getGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seqstate.seq, seqstate.state.toByteArray, groups = groups))) recover {
        case DialogErrors.DialogAlreadyHidden(peer) ⇒
          Error(RpcError(406, "DIALOG_ALREADY_HIDDEN", "Dialog is already hidden.", canTryAgain = false, None))
      }
    }

  override def jhandleShowDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { implicit client ⇒
      (for {
        seqstate ← dialogExt.show(client.userId, peer.asModel)
        groups ← dialogExt.getGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seqstate.seq, seqstate.toByteArray, groups = groups))) recover {
        case DialogErrors.DialogAlreadyHidden(peer) ⇒
          Error(RpcError(406, "DIALOG_ALREADY_SHOWN", "Dialog is already shown.", canTryAgain = false, None))
      }
    }

  override def jhandleLoadHistory(peer: ApiOutPeer, endDate: Long, limit: Int, clientData: ClientData): Future[HandlerResult[ResponseLoadHistory]] =
    authorized(clientData) { implicit client ⇒
      val action = withOutPeer(peer) {
        val modelPeer = peer.asModel
        for {
          historyOwner ← DBIO.from(getHistoryOwner(modelPeer, client.userId))
          (lastReceivedAt, lastReadAt) ← getLastReceiveReadDates(modelPeer)
          messageModels ← HistoryMessageRepo.find(historyOwner, modelPeer, endDateTimeFrom(endDate), limit)
          reactions ← dialogExt.fetchReactions(modelPeer, client.userId, messageModels.map(_.randomId).toSet)

          (messages, userIds) = messageModels.view
            .map(_.ofUser(client.userId))
            .foldLeft(Vector.empty[ApiMessageContainer], Set.empty[Int]) {
              case ((msgs, uids), message) ⇒
                message.asStruct(lastReceivedAt, lastReadAt, reactions.getOrElse(message.randomId, Vector.empty)).toOption match {
                  case Some(messageStruct) ⇒
                    val newMsgs = msgs :+ messageStruct

                    val newUserIds = relatedUsers(messageStruct.message) ++
                      (if (message.senderUserId != client.userId)
                        uids + message.senderUserId
                      else
                        uids)

                    (newMsgs, newUserIds)
                  case None ⇒ (msgs, uids)
                }
            }
          userStructs ← DBIO.from(Future.sequence(userIds.toVector map (userExt.getApiStruct(_, client.userId, client.authId))))
        } yield Ok(ResponseLoadHistory(messages, userStructs))
      }
      db.run(action)
    }

  override def jhandleDeleteMessage(outPeer: ApiOutPeer, randomIds: IndexedSeq[Long], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val action = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(outPeer) {
        val peer = outPeer.asModel

        withHistoryOwner(peer, client.userId) { historyOwner ⇒
          if (isSharedUser(historyOwner)) {
            HistoryMessageRepo.find(historyOwner, peer, randomIds.toSet) flatMap { messages ⇒
              if (messages.exists(_.senderUserId != client.userId)) {
                DBIO.successful(Error(CommonErrors.forbidden("You can only delete your own messages")))
              } else {
                val update = UpdateMessageDelete(outPeer.asPeer, randomIds)

                for {
                  _ ← HistoryMessageRepo.delete(historyOwner, peer, randomIds.toSet)
                  groupUserIds ← GroupUserRepo.findUserIds(peer.id) map (_.toSet)
                  (seqstate, _) ← DBIO.from(userExt.broadcastClientAndUsersUpdate(groupUserIds, update, None, false))
                } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
              }
            }
          } else {
            val update = UpdateMessageDelete(outPeer.asPeer, randomIds)
            for {
              _ ← HistoryMessageRepo.delete(client.userId, peer, randomIds.toSet)
              seqstate ← DBIO.from(userExt.broadcastClientUpdate(update, None, isFat = false))
            } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
          }
        }
      }
    }

    db.run(toDBIOAction(action))
  }

  private val ZeroDate = new DateTime(0)
  private val MaxDate = new DateTime(294276, 1, 1, 0, 0).getMillis

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

  /**
   * returns correct receive and read dates to calculate message states(Sent/Received/Read).
   * in private dialog there are dates in peer's dialog
   * in group dialog there are common dates of group
   */
  private def getLastReceiveReadDates(peer: Peer)(implicit client: AuthorizedClientData): DBIO[(DateTime, DateTime)] = {
    val optDatesAction = peer match {
      case Peer(PeerType.Private, peerUserId) ⇒
        DialogRepo.findUsers(peerUserId, Peer.privat(client.userId)) map (_.map(d ⇒ d.ownerLastReceivedAt → d.ownerLastReadAt))
      case Peer(PeerType.Group, _) ⇒
        DialogRepo.findCommon(None, peer) map (_.map(d ⇒ d.lastReceivedAt → d.lastReadAt))
    }
    optDatesAction map { _ getOrElse (ZeroDate → ZeroDate) }
  }

  private def getDialogStruct(dialogModel: Dialog)(implicit client: AuthorizedClientData): dbio.DBIO[Option[ApiDialog]] = {
    withHistoryOwner(dialogModel.peer, client.userId) { historyOwner ⇒
      for {
        (lastReceivedAt, lastReadAt) ← getLastReceiveReadDates(dialogModel.peer)
        messageOpt ← HistoryMessageRepo.findNewest(historyOwner, dialogModel.peer) map (_.map(_.ofUser(client.userId)))
        reactions ← messageOpt map (m ⇒ dialogExt.fetchReactions(dialogModel.peer, client.userId, m.randomId)) getOrElse DBIO.successful(Vector.empty)
        unreadCount ← dialogExt.getUnreadCount(client.userId, historyOwner, dialogModel.peer, dialogModel.ownerLastReadAt)
      } yield {
        val emptyMessageContent = ApiTextMessage(text = "", mentions = Vector.empty, ext = None)
        val messageModel = messageOpt.getOrElse(HistoryMessage(dialogModel.userId, dialogModel.peer, new DateTime(0), 0, 0, emptyMessageContent.header, emptyMessageContent.toByteArray, None))
        messageModel.asStruct(lastReceivedAt, lastReadAt, reactions).toOption map { message ⇒
          ApiDialog(
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
  }

  private def getDialogsUsersGroups(dialogs: Seq[ApiDialog])(implicit client: AuthorizedClientData) = {
    val (userIds, groupIds) = dialogs.foldLeft((Set.empty[Int], Set.empty[Int])) {
      case ((uacc, gacc), dialog) ⇒
        if (dialog.peer.`type` == ApiPeerType.Private) {
          (uacc ++ relatedUsers(dialog.message) ++ Set(dialog.peer.id, dialog.senderUserId), gacc)
        } else {
          (uacc ++ relatedUsers(dialog.message) + dialog.senderUserId, gacc + dialog.peer.id)
        }
    }

    for {
      groups ← DBIO.from(Future.sequence(groupIds map (groupExt.getApiStruct(_, client.userId))))
      groupUserIds = groups.flatMap(g ⇒ g.members.flatMap(m ⇒ Seq(m.userId, m.inviterUserId)) :+ g.creatorUserId)
      users ← DBIO.from(Future.sequence((userIds ++ groupUserIds).filterNot(_ == 0) map (UserUtils.safeGetUser(_, client.userId, client.authId)))) map (_.flatten)
    } yield (users, groups)
  }

  private def relatedUsers(message: ApiMessage): Set[Int] = {
    message match {
      case ApiServiceMessage(_, extOpt)   ⇒ extOpt map relatedUsers getOrElse Set.empty
      case ApiTextMessage(_, mentions, _) ⇒ mentions.toSet
      case ApiJsonMessage(_)              ⇒ Set.empty
      case _: ApiDocumentMessage          ⇒ Set.empty
      case _: ApiStickerMessage           ⇒ Set.empty
      case _: ApiUnsupportedMessage       ⇒ Set.empty
      case _: ApiBinaryMessage            ⇒ Set.empty
      case _: ApiEncryptedMessage         ⇒ Set.empty
    }
  }

  private def relatedUsers(ext: ApiServiceEx): Set[Int] =
    ext match {
      case ApiServiceExContactRegistered(userId)                  ⇒ Set(userId)
      case ApiServiceExChangedAvatar(_)                           ⇒ Set.empty
      case ApiServiceExChangedTitle(_)                            ⇒ Set.empty
      case ApiServiceExChangedTopic(_)                            ⇒ Set.empty
      case ApiServiceExChangedAbout(_)                            ⇒ Set.empty
      case ApiServiceExGroupCreated | _: ApiServiceExGroupCreated ⇒ Set.empty
      case ApiServiceExPhoneCall(_)                               ⇒ Set.empty
      case ApiServiceExPhoneMissed | _: ApiServiceExPhoneMissed   ⇒ Set.empty
      case ApiServiceExUserInvited(invitedUserId)                 ⇒ Set(invitedUserId)
      case ApiServiceExUserJoined | _: ApiServiceExUserJoined     ⇒ Set.empty
      case ApiServiceExUserKicked(kickedUserId)                   ⇒ Set(kickedUserId)
      case ApiServiceExUserLeft | _: ApiServiceExUserLeft         ⇒ Set.empty
    }
}
