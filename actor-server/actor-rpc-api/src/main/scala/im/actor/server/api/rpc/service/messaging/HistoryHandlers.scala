package im.actor.server.api.rpc.service.messaging

import java.time.Instant

import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.server.dialog.HistoryUtils
import im.actor.server.group.CanSendMessageInfo
import im.actor.server.model.Peer
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.SeqState
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.language.postfixOps

trait HistoryHandlers {
  self: MessagingServiceImpl ⇒

  import HistoryUtils._
  import EntitiesHelpers._
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
    authorized(clientData) { client ⇒
      val canClearChat = peer.`type` match {
        case ApiPeerType.Private | ApiPeerType.EncryptedPrivate ⇒
          FastFuture.successful(true)
        case ApiPeerType.Group ⇒
          groupExt.isHistoryShared(peer.id) map (isShared ⇒ !isShared)
      }

      canClearChat flatMap { canClear ⇒
        if (canClear) {
          for {
            _ ← db.run(HistoryMessageRepo.deleteAll(client.userId, peer.asModel))
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              client.userId,
              client.authId,
              update = UpdateChatClear(peer.asPeer)
            )
          } yield Ok(ResponseSeq(seq, state.toByteArray))
        } else {
          FastFuture.successful[HandlerResult[ResponseSeq]](
            Error(CommonRpcErrors.forbidden("Can't clear chat with shared history"))
          )
        }
      }
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
      val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
      val loadGroupMembers = !optimizations.contains(ApiUpdateOptimization.GROUPS_V2)

      for {
        (dialogs, nextOffset) ← dialogExt.fetchArchivedApiDialogs(client.userId, offset, limit)
        ((users, userPeers), (groups, groupPeers)) ← usersAndGroupsByDialogs(dialogs.toSeq, stripEntities, loadGroupMembers)
      } yield Ok(ResponseLoadArchived(
        dialogs = dialogs.toVector,
        nextOffset = nextOffset,
        groups = groups,
        users = users,
        userPeers = userPeers,
        groupPeers = groupPeers
      ))
    }

  override def doHandleLoadDialogs(
    endDate:       Long,
    limit:         Int,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadDialogs]] =
    authorized(clientData) { implicit client ⇒
      val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
      val loadGroupMembers = !optimizations.contains(ApiUpdateOptimization.GROUPS_V2)

      for {
        dialogs ← dialogExt.fetchApiDialogs(client.userId, Instant.ofEpochMilli(endDate), limit)
        ((users, userPeers), (groups, groupPeers)) ← usersAndGroupsByDialogs(dialogs.toSeq, stripEntities, loadGroupMembers)
      } yield Ok(ResponseLoadDialogs(
        groups = groups,
        users = users,
        dialogs = dialogs.toVector,
        userPeers = userPeers,
        groupPeers = groupPeers
      ))
    }

  override def doHandleLoadGroupedDialogs(
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseLoadGroupedDialogs]] =
    authorized(clientData) { implicit client ⇒
      val stripEntities = optimizations contains ApiUpdateOptimization.STRIP_ENTITIES
      val loadGroupMembers = !optimizations.contains(ApiUpdateOptimization.GROUPS_V2)

      for {
        dialogGroups ← dialogExt.fetchApiGroupedDialogs(client.userId)
        ((users, userPeers), (groups, groupPeers)) ← usersAndGroupsByShortDialogs(
          dialogs = dialogGroups.flatMap(_.dialogs),
          stripEntities,
          loadGroupMembers
        )
        archivedExist ← dialogExt.fetchArchivedDialogs(client.userId, None, 1) map (_._1.nonEmpty)
        showInvite ← db.run(UserContactRepo.count(client.userId)) map (_ < 5)
      } yield Ok(ResponseLoadGroupedDialogs(
        dialogs = dialogGroups,
        users = users,
        groups = groups,
        showArchived = Some(archivedExist),
        showInvite = Some(showInvite),
        userPeers = userPeers,
        groupPeers = groupPeers
      ))
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
      withOutPeer(peer) {
        val modelPeer = peer.asModel
        val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)
        val loadGroupMembers = !optimizations.contains(ApiUpdateOptimization.GROUPS_V2)

        val action = for {
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
          ((users, userPeers), (groups, groupPeers)) ← DBIO.from(usersAndGroupsByIds(groupIds, userIds, stripEntities, loadGroupMembers))
        } yield Ok(ResponseLoadHistory(
          history = messages,
          users = users,
          userPeers = userPeers,
          groups = groups,
          groupPeers = groupPeers
        ))
        db.run(action)
      }
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

}
