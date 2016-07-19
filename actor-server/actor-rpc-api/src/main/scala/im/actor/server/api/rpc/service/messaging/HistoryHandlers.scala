package im.actor.server.api.rpc.service.messaging

import java.time.Instant

import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiEmptyMessage, _ }
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiOutPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.server.dialog.HistoryUtils
import im.actor.server.group.GroupQueries.CanSendMessageResponse
import im.actor.server.group.{ CanSendMessageInfo, GroupUtils }
import im.actor.server.model.{ DialogObsolete, HistoryMessage, Peer, PeerType }
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.{ GroupUserRepo, HistoryMessageRepo }
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserUtils
import org.joda.time.DateTime
import slick.dbio
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.language.postfixOps

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import DBIOResultRpc._
  import HistoryUtils._
  import Implicits._

  private val CantDelete = Error(CommonRpcErrors.forbidden("You can't delete these messages"))

  override def doHandleMessageReceived(peer: ApiOutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      dialogExt.messageReceived(peer.asPeer, client.userId, date) map (_ ⇒ Ok(ResponseVoid))
    }
  }

  override def doHandleMessageRead(peer: ApiOutPeer, date: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      dialogExt.messageRead(peer.asPeer, client.userId, client.authId, date) map (_ ⇒ Ok(ResponseVoid))
    }
  }

  override def doHandleClearChat(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      val update = UpdateChatClear(peer.asPeer)

      val action = (for {
        _ ← fromDBIOBoolean(CommonRpcErrors.forbidden("Clearing of public chats is forbidden")) {
          if (peer.`type` == ApiPeerType.Private) {
            DBIO.successful(true)
          } else {
            DBIO.from(groupExt.isHistoryShared(peer.id)) flatMap (isHistoryShared ⇒ DBIO.successful(!isHistoryShared))
          }
        }
        _ ← fromDBIO(HistoryMessageRepo.deleteAll(client.userId, peer.asModel))
        seqState ← fromFuture(seqUpdExt.deliverClientUpdate(
          client.userId,
          client.authId,
          update,
          pushRules = seqUpdExt.pushRules(isFat = false, None)
        ))
      } yield ResponseSeq(seqState.seq, seqState.state.toByteArray)).value
      db.run(action)
    }

  override def doHandleDeleteChat(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← dialogExt.delete(client.userId, client.authId, peer.asModel)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }
  }

  override def doHandleLoadArchived(
    offset:        Option[Array[Byte]],
    limit:         Int,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadArchived]] =
    authorized(clientData) { implicit client ⇒
      for {
        (dialogs, nextOffset) ← dialogExt.fetchArchivedApiDialogs(client.userId, offset, limit)
        (users, groups) ← db.run(getDialogsUsersGroups(dialogs.toSeq))
      } yield {
        val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
        Ok(ResponseLoadArchived(
          dialogs = dialogs.toVector,
          nextOffset = nextOffset,
          groups = if (stripEntities) Vector.empty else groups.toVector,
          users = if (stripEntities) Vector.empty else users.toVector,
          userPeers = users.toVector map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)),
          groupPeers = groups.toVector map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash))
        ))
      }
    }

  override def doHandleLoadDialogs(
    endDate:       Long,
    limit:         Int,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadDialogs]] =
    authorized(clientData) { implicit client ⇒
      for {
        dialogs ← dialogExt.fetchApiDialogs(client.userId, Instant.ofEpochMilli(endDate), limit)
        (users, groups) ← db.run(getDialogsUsersGroups(dialogs.toSeq))
      } yield {
        val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

        Ok(ResponseLoadDialogs(
          groups = if (stripEntities) Vector.empty else groups.toVector,
          users = if (stripEntities) Vector.empty else users.toVector,
          dialogs = dialogs.toVector,
          userPeers = users.toVector map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)),
          groupPeers = groups.toVector map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash))
        ))
      }
    }

  override def doHandleLoadGroupedDialogs(
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadGroupedDialogs]] =
    authorized(clientData) { implicit client ⇒
      for {
        dialogGroups ← dialogExt.fetchApiGroupedDialogs(client.userId)
        (userIds, groupIds) = dialogGroups.view.flatMap(_.dialogs).foldLeft((Seq.empty[Int], Seq.empty[Int])) {
          case ((uids, gids), dialog) ⇒
            dialog.peer.`type` match {
              case ApiPeerType.Group   ⇒ (uids, gids :+ dialog.peer.id)
              case ApiPeerType.Private ⇒ (uids :+ dialog.peer.id, gids)
            }
        }
        (groups, users) ← GroupUtils.getGroupsUsers(groupIds, userIds, client.userId, client.authId)
        archivedExist ← dialogExt.fetchArchivedDialogs(client.userId, None, 1) map (_._1.nonEmpty)
        showInvite ← db.run(UserContactRepo.count(client.userId)) map (_ < 5)
      } yield {
        val stripEntities = optimizations contains ApiUpdateOptimization.STRIP_ENTITIES

        Ok(ResponseLoadGroupedDialogs(
          dialogs = dialogGroups,
          users = if (stripEntities) Vector.empty else users.toVector,
          groups = if (stripEntities) Vector.empty else groups.toVector,
          showArchived = Some(archivedExist),
          showInvite = Some(showInvite),
          userPeers = if (stripEntities) users.toVector map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)) else Vector.empty,
          groupPeers = if (stripEntities) groups.toVector map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash)) else Vector.empty
        ))
      }
    }

  override def doHandleHideDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { implicit client ⇒
      for {
        seqState ← dialogExt.archive(client.userId, client.authId, peer.asModel)
        groups ← dialogExt.fetchApiGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seqState.seq, seqState.state.toByteArray, groups = groups))
    }

  override def doHandleShowDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { implicit client ⇒
      for {
        seqState ← dialogExt.unarchive(client.userId, client.authId, peer.asModel)
        groups ← dialogExt.fetchApiGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seqState.seq, seqState.toByteArray, groups = groups))
    }

  override def doHandleArchiveChat(
    peer:       im.actor.api.rpc.peers.ApiOutPeer,
    clientData: im.actor.api.rpc.ClientData
  ): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withOutPeer(peer) {
        for (seqState ← dialogExt.archive(client.userId, client.authId, peer.asModel))
          yield Ok(ResponseSeq(seqState.seq, seqState.state.toByteArray))
      }
    }

  override def doHandleLoadHistory(
    peer:          ApiOutPeer,
    date:          Long,
    mode:          Option[ApiListLoadMode.Value],
    limit:         Int,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadHistory]] =
    authorized(clientData) { implicit client ⇒
      val action = withOutPeerDBIO(peer) {
        val modelPeer = peer.asModel
        for {
          historyOwner ← DBIO.from(getHistoryOwner(modelPeer, client.userId))
          (lastReceivedAt, lastReadAt) ← getLastReceiveReadDates(modelPeer)
          messageModels ← mode match {
            case Some(ApiListLoadMode.Forward)  ⇒ HistoryMessageRepo.findAfter(historyOwner, modelPeer, dateTimeFrom(date), limit.toLong)
            case Some(ApiListLoadMode.Backward) ⇒ HistoryMessageRepo.findBefore(historyOwner, modelPeer, dateTimeFrom(date), limit.toLong)
            case Some(ApiListLoadMode.Both)     ⇒ HistoryMessageRepo.findBidi(historyOwner, modelPeer, dateTimeFrom(date), limit.toLong)
            case _                              ⇒ HistoryMessageRepo.find(historyOwner, modelPeer, endDateTimeFrom(date), limit)
          }
          reactions ← dialogExt.fetchReactions(modelPeer, client.userId, messageModels.map(_.randomId).toSet)

          (messages, userIds, groupIds) = messageModels.view
            .map(_.ofUser(client.userId))
            .foldLeft(Vector.empty[ApiMessageContainer], Set.empty[Int], Set.empty[Int]) {
              case ((msgs, uids, guids), message) ⇒
                message.asStruct(lastReceivedAt, lastReadAt, reactions.getOrElse(message.randomId, Vector.empty)).toOption match {
                  case Some(messageStruct) ⇒
                    val newMsgs = msgs :+ messageStruct

                    val newUserIds = relatedUsers(messageStruct.message) ++
                      (if (message.senderUserId != client.userId)
                        uids + message.senderUserId
                      else
                        uids)

                    (newMsgs, newUserIds, guids ++ messageStruct._relatedGroupIds)
                  case None ⇒ (msgs, uids, guids)
                }
            }
          users ← DBIO.from(Future.sequence(userIds.toVector map (userExt.getApiStruct(_, client.userId, client.authId))))
          groups ← DBIO.from(Future.sequence(groupIds.toVector map (groupExt.getApiStruct(_, client.userId))))
        } yield {
          val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

          Ok(ResponseLoadHistory(
            history = messages,
            users = if (stripEntities) Vector.empty else users,
            userPeers = users map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)),
            groups = if (stripEntities) Vector.empty else groups,
            groupPeers = groups map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash))
          ))
        }
      }
      db.run(action)
    }

  override def doHandleDeleteMessage(outPeer: ApiOutPeer, randomIds: IndexedSeq[Long], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withOutPeer(outPeer) {
        val peer = outPeer.asModel
        getHistoryOwner(peer, client.userId) flatMap { historyOwner ⇒
          if (isSharedUser(historyOwner)) {
            for {
              CanSendMessageInfo(canSend, isChannel, memberIds, _) ← groupExt.canSendMessage(peer.id, client.userId)
              result ← (isChannel, canSend) match {
                case (true, true) ⇒ // channel, client user is one of those who can send messages, thus he can also delete them.
                  deleteMessages(peer, historyOwner, randomIds, memberIds)
                case (true, false) ⇒ // channel, client user can't send messages, thus he can't delete them.
                  FastFuture.successful(CantDelete)
                case (false, _) ⇒ // not a channel group. Must check if user deletes only messages he sent.
                  for {
                    messages ← db.run(HistoryMessageRepo.find(historyOwner, peer, randomIds.toSet)) // TODO: rewrite to ids check only
                    res ← if (messages.forall(_.senderUserId == client.userId)) {
                      deleteMessages(peer, historyOwner, randomIds, memberIds)
                    } else {
                      FastFuture.successful(CantDelete)
                    }
                  } yield res
              }
            } yield result
          } else {
            deleteMessages(peer, historyOwner, randomIds, otherUsersIds = Set.empty)
          }
        }
      }
    }

  private def deleteMessages(peer: Peer, historyOwner: Int, randomIds: IndexedSeq[Long], otherUsersIds: Set[Int])(implicit client: AuthorizedClientData) =
    for {
      _ ← db.run(HistoryMessageRepo.delete(historyOwner, peer, randomIds.toSet))
      SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
        client.userId,
        client.authId,
        bcastUserIds = otherUsersIds,
        update = UpdateMessageDelete(peer.asStruct, randomIds),
        pushRules = seqUpdExt.pushRules(isFat = false, None, excludeAuthIds = Seq(client.authId))
      )
    } yield Ok(ResponseSeq(seq, state.toByteArray))

  private val MaxDateTime = new DateTime(294276, 1, 1, 0, 0)
  private val MaxDate = MaxDateTime.getMillis

  private def endDateTimeFrom(date: Long): Option[DateTime] = {
    if (date == 0l) {
      None
    } else {
      Some(new DateTime(
        if (date >= MaxDate)
          MaxDateTime
        else
          date
      ))
    }
  }

  private def dateTimeFrom(date: Long): DateTime =
    if (date >= MaxDate) MaxDateTime else new DateTime(date)

  /**
   * returns correct receive and read dates to calculate message states(Sent/Received/Read).
   * in private dialog there are dates in peer's dialog
   * in group dialog there are common dates of group
   */
  private def getLastReceiveReadDates(peer: Peer)(implicit client: AuthorizedClientData): DBIO[(DateTime, DateTime)] = {
    DBIO.from(for {
      info ← dialogExt.getDialogInfo(client.userId, peer)
    } yield (new DateTime(info.lastReceivedDate.toEpochMilli), new DateTime(info.lastReadDate.toEpochMilli)))
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
      case _: ApiEmptyMessage             ⇒ Set.empty
      case _: ApiDocumentMessage          ⇒ Set.empty
      case _: ApiStickerMessage           ⇒ Set.empty
      case _: ApiUnsupportedMessage       ⇒ Set.empty
      case _: ApiBinaryMessage            ⇒ Set.empty
      case _: ApiEncryptedMessage         ⇒ Set.empty
    }
  }

  private def relatedUsers(ext: ApiServiceEx): Set[Int] =
    ext match {
      case ApiServiceExContactRegistered(userId)                     ⇒ Set(userId)
      case ApiServiceExChangedAvatar(_)                              ⇒ Set.empty
      case ApiServiceExChangedTitle(_)                               ⇒ Set.empty
      case ApiServiceExChangedTopic(_)                               ⇒ Set.empty
      case ApiServiceExChangedAbout(_)                               ⇒ Set.empty
      case ApiServiceExGroupCreated | _: ApiServiceExGroupCreated    ⇒ Set.empty
      case ApiServiceExPhoneCall(_)                                  ⇒ Set.empty
      case ApiServiceExPhoneMissed | _: ApiServiceExPhoneMissed      ⇒ Set.empty
      case ApiServiceExUserInvited(invitedUserId)                    ⇒ Set(invitedUserId)
      case ApiServiceExUserJoined | _: ApiServiceExUserJoined        ⇒ Set.empty
      case ApiServiceExUserKicked(kickedUserId)                      ⇒ Set(kickedUserId)
      case ApiServiceExUserLeft | _: ApiServiceExUserLeft            ⇒ Set.empty
      case _: ApiServiceExChatArchived | _: ApiServiceExChatRestored ⇒ Set.empty
    }
}
