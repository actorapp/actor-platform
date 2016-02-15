package im.actor.server.api.rpc.service.sequence

import akka.event.Logging
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.api.rpc.sequence._
import im.actor.server.acl.ACLUtils
import im.actor.server.model.SeqUpdate

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

import akka.actor.ActorSystem
import akka.stream.Materializer

import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType, ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupUtils, GroupExtension }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.session._
import im.actor.server.user.{ UserExtension, UserUtils }
import im.actor.server.db.ActorPostgresDriver.api._

final class SequenceServiceImpl(config: SequenceServiceConfig)(
  implicit
  sessionRegion: SessionRegion,
  actorSystem:   ActorSystem,
  materializer:  Materializer
) extends SequenceService {
  import FutureResultRpcCats._

  protected override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val log = Logging(actorSystem, getClass)
  private val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  private val maxDifferenceSize: Long = config.maxDifferenceSize

  private def subscribeToSeq(opts: Seq[ApiUpdateOptimization.Value])(implicit client: AuthorizedClientData): Unit = {
    sessionRegion.ref ! SessionEnvelope(client.authId, client.sessionId)
      .withSubscribeToSeq(SubscribeToSeq(opts map (_.id)))
  }

  override def doHandleGetState(optimizations: IndexedSeq[ApiUpdateOptimization.ApiUpdateOptimization], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      subscribeToSeq(optimizations)
      for {
        seqstate ← DBIO.from(seqUpdExt.getSeqState(client.userId))
      } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def doHandleGetDifference(seq: Int, state: Array[Byte], optimizations: IndexedSeq[ApiUpdateOptimization.ApiUpdateOptimization], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    authorized(clientData) { implicit client ⇒
      subscribeToSeq(optimizations)

      val seqDeltaFuture =
        if (state.nonEmpty) {
          getDelta(client.userId, client.authId) andThen {
            case Success(delta) ⇒ log.debug("Delta for client: {} is: {}", client, delta)
          }
        } else Future.successful(0)

      for {
        seqDelta ← seqDeltaFuture
        // FIXME: would new updates between getSeqState and getDifference break client state?
        (updates, needMore) ← seqUpdExt.getDifference(client.userId, seq + seqDelta, client.authSid, maxDifferenceSize)
        (diffUpdates, userIds, groupIds) = extractDiff(updates)
        (users, groups) ← getUsersGroups(userIds, groupIds, optimizations)

        (userRefs, groupRefs) ← getRefs(userIds, groupIds, optimizations, client)
      } yield {
        val newSeq = updates.lastOption match {
          case Some(upd) ⇒ upd.seq
          case None      ⇒ seq + seqDelta
        }

        Ok(ResponseGetDifference(
          seq = newSeq,
          state = Array.empty,
          updates = diffUpdates,
          needMore = needMore,
          users = users.toVector,
          groups = groups.toVector,
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
        _ ← fromFutureBoolean(CommonRpcErrors.InvalidAccessHash)(ACLUtils.checkOutPeers(users, client.authId))
        _ ← fromFutureBoolean(CommonRpcErrors.InvalidAccessHash)(ACLUtils.checkOutPeers(groups))
        res ← fromFuture(GroupUtils.getGroupsUsers(
          groups map (_.groupId),
          users map (_.userId), client.userId, client.authId
        ))
      } yield {
        val (groupStructs, userStructs) = res
        ResponseGetReferencedEntitites(userStructs.toVector, groupStructs.toVector)
      }).value map (_.toScalaz)
    }

  override def doHandleSubscribeToOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction)) andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToOnline(SubscribeToOnline(userIds.toSeq))
    }
  }

  override def doHandleSubscribeFromOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction)) andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeFromOnline(SubscribeFromOnline(userIds.toSeq))
    }
  }

  override def doHandleSubscribeToGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToGroupOnline(SubscribeToGroupOnline(groups.map(_.groupId)))
    }
  }

  override def doHandleSubscribeFromGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeFromGroupOnline(SubscribeFromGroupOnline(groups.map(_.groupId)))
    }
  }

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

  private def extractDiff(updates: IndexedSeq[SeqUpdate])(implicit client: AuthorizedClientData): (IndexedSeq[ApiUpdateContainer], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[ApiUpdateContainer], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) ⇒
        val upd = update.getMapping.custom.getOrElse(client.authSid, update.getMapping.getDefault)

        (updates :+ ApiUpdateContainer(upd.header, upd.body.toByteArray),
          userIds ++ upd.userIds,
          groupIds ++ upd.groupIds)
    }
  }

  private def getUsersGroups(
    userIds:       Set[Int],
    groupIds:      Set[Int],
    optimizations: Seq[ApiUpdateOptimization.Value]
  )(implicit client: AuthorizedClientData): Future[(Vector[ApiUser], Vector[ApiGroup])] = {
    if (optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES))
      Future.successful((Vector.empty, Vector.empty))
    else
      for {
        groups ← Future.sequence(groupIds.toVector map (GroupExtension(actorSystem).getApiStruct(_, client.userId)))
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
      Future.successful((Vector.empty, Vector.empty))
  }
}
