package im.actor.server.api.rpc.service.sequence

import akka.event.Logging
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.api.rpc.sequence._
import im.actor.server.acl.ACLUtils
import im.actor.server.model.SerializedUpdate

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success
import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.concurrent.FutureExt
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.sequence.{ Difference, SeqState, SeqUpdatesExtension }
import im.actor.server.session._
import im.actor.server.user.UserUtils
import im.actor.server.db.ActorPostgresDriver.api._

final class SequenceServiceImpl(config: SequenceServiceConfig)(
  implicit
  sessionRegion: SessionRegion,
  actorSystem:   ActorSystem
) extends SequenceService {
  import FutureResultRpc._
  import PeerHelpers._

  protected override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val log = Logging(actorSystem, getClass)
  private val db: Database = DbExtension(actorSystem).db
  private val seqUpdExt = SeqUpdatesExtension(actorSystem)
  private val groupExt = GroupExtension(actorSystem)

  private val maxDifferenceSize: Long = config.maxDifferenceSize

  private def subscribeToSeq(opts: Seq[ApiUpdateOptimization.Value])(implicit client: AuthorizedClientData): Unit = {
    val optsIds = opts map (_.id)
    seqUpdExt.addOptimizations(client.userId, client.authId, optsIds)
    sessionRegion.ref ! SessionEnvelope(client.authId, client.sessionId)
      .withSubscribeToSeq(SubscribeToSeq(optsIds))
  }

  override def doHandleGetState(optimizations: IndexedSeq[ApiUpdateOptimization.ApiUpdateOptimization], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      subscribeToSeq(optimizations)
      for {
        SeqState(seq, state) ← seqUpdExt.getSeqState(client.userId, clientData.authId)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleGetDifference(
    clientSeq:     Int,
    commonState:   Array[Byte],
    optimizations: IndexedSeq[ApiUpdateOptimization.ApiUpdateOptimization],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseGetDifference]] = {
    authorized(clientData) { implicit client ⇒
      subscribeToSeq(optimizations)

      for {
        // FIXME: would new updates between getSeqState and getDifference break client state?
        Difference(updates, newClientSeq, newCommonState, needMore) ← seqUpdExt.getDifference(
          userId = client.userId,
          clientSeq = clientSeq,
          commonState = commonState,
          authId = client.authId,
          authSid = client.authSid,
          maxSizeInBytes = maxDifferenceSize
        )
        (diffUpdates, userIds, groupIds) = extractDiff(updates)
        (users, groups) ← getUsersGroups(userIds + client.userId, groupIds, optimizations)

        (userRefs, groupRefs) ← getRefs(userIds, groupIds, optimizations, client)
      } yield {
        Ok(ResponseGetDifference(
          seq = newClientSeq,
          state = newCommonState,
          updates = diffUpdates,
          needMore = needMore,
          users = users,
          groups = groups,
          messages = Vector.empty,
          usersRefs = userRefs,
          groupsRefs = groupRefs
        ))
      }
    }
  }

  override def doHandleGetReferencedEntitites(
    users:      IndexedSeq[ApiUserOutPeer],
    groups:     IndexedSeq[ApiGroupOutPeer],
    clientData: ClientData
  ): Future[HandlerResult[ResponseGetReferencedEntitites]] =
    authorized(clientData) { client ⇒
      (for {
        // check access hash only for private groups.
        // No need to check access hash for public groups.
        privateGroups ← fromFuture((groups foldLeft FastFuture.successful(Vector.empty[ApiGroupOutPeer])) {
          case (accFu, el) ⇒
            for {
              acc ← accFu
              isShared ← groupExt.isHistoryShared(el.groupId)
            } yield if (isShared) acc else acc :+ el
        })
        _ ← fromFutureBoolean(CommonRpcErrors.InvalidAccessHash)(ACLUtils.checkUserOutPeers(users, client.authId))
        _ ← fromFutureBoolean(CommonRpcErrors.InvalidAccessHash)(ACLUtils.checkGroupOutPeers(privateGroups))
        res ← fromFuture(GroupUtils.getGroupsUsers(
          groups map (_.groupId),
          users map (_.userId), client.userId, client.authId
        ))
      } yield {
        val (groupStructs, userStructs) = res
        ResponseGetReferencedEntitites(userStructs.toVector, groupStructs.toVector)
      }).value
    }

  override def doHandleSubscribeToOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = authorized(clientData) { client ⇒ FastFuture.successful(Ok(ResponseVoid)) }

    authorizedAction andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToOnline(SubscribeToOnline(userIds.toSeq))
    }
  }

  override def doHandleSubscribeFromOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = authorized(clientData) { client ⇒ FastFuture.successful(Ok(ResponseVoid)) }

    authorizedAction andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeFromOnline(SubscribeFromOnline(userIds.toSeq))
    }
  }

  override def doHandleSubscribeToGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    withGroupOutPeers(groups) {
      FastFuture.successful(Ok(ResponseVoid)) andThen {
        case _ ⇒
          getNonChannelsIds(groups) foreach { groupIds ⇒
            sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
              .withSubscribeToGroupOnline(SubscribeToGroupOnline(groupIds))
          }
      }
    }

  override def doHandleSubscribeFromGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    withGroupOutPeers(groups) {
      FastFuture.successful(Ok(ResponseVoid)) andThen {
        case _ ⇒
          getNonChannelsIds(groups) foreach { groupIds ⇒
            sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
              .withSubscribeFromGroupOnline(SubscribeFromGroupOnline(groupIds))
          }

      }
    }

  // TODO: add non deleted check too.
  private def getNonChannelsIds(groups: Seq[ApiGroupOutPeer]): Future[Seq[Int]] = {
    FutureExt.ftraverse(groups) {
      case ApiGroupOutPeer(groupId, _) ⇒
        groupExt.isChannel(groupId) map (isChannel ⇒ if (isChannel) None else Some(groupId))
    } map (_.flatten)
  }

  //TODO: remove
  private def getDelta(userId: Int, authId: Long): Future[Int] =
    db.run(for {
      maxSeq ← getMaxSeq(userId)
      seq ← getSeq(authId)
    } yield maxSeq - seq)

  private def getMaxSeq(userId: Int): DBIO[Int] = {
    sql"""SELECT seq FROM user_sequence WHERE user_id = $userId ORDER BY seq DESC LIMIT 1""".as[Int].headOption map (_ getOrElse 0)
  }

  private def getSeq(authId: Long)(implicit ec: ExecutionContext) =
    sql"""SELECT seq FROM seq_updates_ngen WHERE auth_id = $authId ORDER BY timestamp DESC LIMIT 1""".as[Int].headOption map (_ getOrElse 0)
  //

  private def extractDiff(updates: IndexedSeq[SerializedUpdate]): (IndexedSeq[ApiUpdateContainer], Set[Int], Set[Int]) =
    updates.foldLeft[(Vector[ApiUpdateContainer], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updatesAcc, userIds, groupIds), update) ⇒
        (updatesAcc :+ ApiUpdateContainer(update.header, update.body.toByteArray),
          userIds ++ update.userIds,
          groupIds ++ update.groupIds)
    }

  private def getUsersGroups(
    userIds:       Set[Int],
    groupIds:      Set[Int],
    optimizations: Seq[ApiUpdateOptimization.Value]
  )(implicit client: AuthorizedClientData): Future[(Vector[ApiUser], Vector[ApiGroup])] = {
    if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES))
      FastFuture.successful((Vector.empty, Vector.empty))
    else
      for {
        groups ← Future.sequence(groupIds.toVector map (groupExt.getApiStruct(_, client.userId)))
        // TODO: #perf optimize collection operations
        allUserIds = userIds ++ groups.foldLeft(Vector.empty[Int]) { (ids, g) ⇒ ids ++ g.members.flatMap(m ⇒ Vector(m.userId, m.inviterUserId)) :+ g.creatorUserId }
        users ← Future.sequence(allUserIds.toVector map (UserUtils.safeGetUser(_, client.userId, client.authId))) map (_.flatten)
      } yield (users, groups)
  }

  private def getRefs(
    userIds:       Set[Int],
    groupIds:      Set[Int],
    optimizations: IndexedSeq[ApiUpdateOptimization.ApiUpdateOptimization],
    client:        AuthorizedClientData
  ): Future[(Vector[ApiUserOutPeer], Vector[ApiGroupOutPeer])] = {
    if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES))
      for {
        us ← Future.sequence(
          userIds.toVector map (id ⇒ ACLUtils.getUserOutPeer(id, client.authId))
        )
        gs ← Future.sequence(
          groupIds.toVector map (id ⇒ ACLUtils.getGroupOutPeer(id))
        )
      } yield (us, gs)
    else
      FastFuture.successful((Vector.empty, Vector.empty))
  }
}
