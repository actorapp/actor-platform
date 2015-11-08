package im.actor.server.api.rpc.service.sequence

import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.server.model.SeqUpdate

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

import akka.actor.ActorSystem
import akka.stream.Materializer
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.api.rpc.sequence.{ ApiDifferenceUpdate, ResponseGetDifference, SequenceService }
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.sequence.{ SeqUpdatesExtension }
import im.actor.server.session._
import im.actor.server.user.UserUtils

final class SequenceServiceImpl(config: SequenceServiceConfig)(
  implicit
  sessionRegion: SessionRegion,
  actorSystem:   ActorSystem,
  materializer:  Materializer
) extends SequenceService {

  protected override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)

  private val maxDifferenceSize: Long = config.maxDifferenceSize

  private def subscribeToSeq()(implicit client: AuthorizedClientData): Unit = {
    sessionRegion.ref ! SessionEnvelope(client.authId, client.sessionId)
      .withSubscribeToSeq(SubscribeToSeq())
  }

  override def jhandleGetState(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      subscribeToSeq()
      for {
        seqstate ← DBIO.from(seqUpdExt.getSeqState(client.userId))
      } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetDifference(seq: Int, state: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    authorized(clientData) { implicit client ⇒
      subscribeToSeq()
      for {
        // FIXME: would new updates between getSeqState and getDifference break client state?
        (updates, needMore) ← seqUpdExt.getDifference(client.userId, seq, client.authSid, maxDifferenceSize)
        (diffUpdates, userIds, groupIds) = extractDiff(updates)
        (users, groups) ← getUsersGroups(userIds, groupIds)
      } yield {
        val newSeq = updates.lastOption map (_.seq) getOrElse seq

        Ok(ResponseGetDifference(
          seq = newSeq,
          state = state,
          updates = diffUpdates,
          needMore = needMore,
          users = users.toVector,
          groups = groups.toVector
        ))
      }
    }
  }

  override def jhandleSubscribeToOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
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

  override def jhandleSubscribeFromOnline(users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
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

  override def jhandleSubscribeToGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToGroupOnline(SubscribeToGroupOnline(groups.map(_.groupId)))
    }
  }

  override def jhandleSubscribeFromGroupOnline(groups: IndexedSeq[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeFromGroupOnline(SubscribeFromGroupOnline(groups.map(_.groupId)))
    }
  }

  private def extractDiff(updates: IndexedSeq[SeqUpdate])(implicit client: AuthorizedClientData): (IndexedSeq[ApiDifferenceUpdate], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[ApiDifferenceUpdate], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) ⇒
        val upd = update.getMapping.custom.getOrElse(client.authSid, update.getMapping.getDefault)

        (updates :+ ApiDifferenceUpdate(upd.header, upd.body.toByteArray),
          userIds ++ upd.userIds,
          groupIds ++ upd.groupIds)
    }
  }

  private def getUsersGroups(userIds: Set[Int], groupIds: Set[Int])(implicit client: AuthorizedClientData): Future[(Set[ApiUser], Set[ApiGroup])] = {
    for {
      groups ← Future.sequence(groupIds map (GroupExtension(actorSystem).getApiStruct(_, client.userId)))
      // TODO: #perf optimize collection operations
      allUserIds = userIds ++ groups.foldLeft(Set.empty[Int]) { (ids, g) ⇒ ids ++ g.members.flatMap(m ⇒ Seq(m.userId, m.inviterUserId)) + g.creatorUserId }
      users ← Future.sequence(allUserIds map (UserUtils.safeGetUser(_, client.userId, client.authId))) map (_.flatten)
    } yield (users, groups)
  }
}
