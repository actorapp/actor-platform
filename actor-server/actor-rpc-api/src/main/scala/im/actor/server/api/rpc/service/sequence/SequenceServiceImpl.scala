package im.actor.server.api.rpc.service.sequence

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.api.rpc.sequence.{ ApiDifferenceUpdate, ResponseGetDifference, SequenceService }
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupViewRegion, GroupExtension, GroupOffice }
import im.actor.server.models
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqUpdatesManager }
import im.actor.server.session._
import im.actor.server.user.{ UserViewRegion, UserExtension, UserOffice }

final class SequenceServiceImpl(config: SequenceServiceConfig)(
  implicit
  sessionRegion: SessionRegion,
  actorSystem:   ActorSystem,
  materializer:  Materializer
) extends SequenceService {

  import SeqUpdatesManager._

  protected override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = Timeout(30.seconds)

  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  private implicit val userViewRegion: UserViewRegion = UserExtension(actorSystem).viewRegion
  private implicit val groupViewRegion: GroupViewRegion = GroupExtension(actorSystem).viewRegion

  private val maxDifferenceSize: Long = config.maxDifferenceSize

  override def jhandleGetState(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        seqstate ← DBIO.from(getSeqState(client.authId))
      } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetDifference(seq: Int, state: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒

      for {
        // FIXME: would new updates between getSeqState and getDifference break client state?
        (updates, needMore) ← getDifference(client.authId, bytesToTimestamp(state), maxDifferenceSize)
        (diffUpdates, userIds, groupIds) = extractDiff(updates)
        (users, groups) ← getUsersGroups(userIds, groupIds)
      } yield {
        val (newSeq, newState) = updates.lastOption map { u ⇒ u.seq → timestampToBytes(u.timestamp) } getOrElse (seq → state)

        Ok(ResponseGetDifference(
          seq = newSeq,
          state = newState,
          updates = diffUpdates,
          needMore = needMore,
          users = users.toVector,
          groups = groups.toVector
        ))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSubscribeToOnline(users: Vector[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction)) andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToOnline((SubscribeToOnline(userIds.toSeq)))
    }
  }

  override def jhandleSubscribeFromOnline(users: Vector[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
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

  override def jhandleSubscribeToGroupOnline(groups: Vector[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeToGroupOnline(SubscribeToGroupOnline(groups.map(_.groupId)))
    }
  }

  override def jhandleSubscribeFromGroupOnline(groups: Vector[ApiGroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId)
          .withSubscribeFromGroupOnline(SubscribeFromGroupOnline(groups.map(_.groupId)))
    }
  }

  private def extractDiff(updates: Vector[models.sequence.SeqUpdate]): (Vector[ApiDifferenceUpdate], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[ApiDifferenceUpdate], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) ⇒
        (updates :+ ApiDifferenceUpdate(update.header, update.serializedData),
          userIds ++ update.userIds,
          groupIds ++ update.groupIds)
    }
  }

  private def getUsersGroups(userIds: Set[Int], groupIds: Set[Int])(implicit client: AuthorizedClientData) = {
    DBIO.from(for {
      groups ← Future.sequence(groupIds map (GroupOffice.getApiStruct(_, client.userId)))
      // TODO: #perf optimize collection operations
      allUserIds = userIds ++ groups.foldLeft(Set.empty[Int]) { (ids, g) ⇒ ids ++ g.members.map(m ⇒ Seq(m.userId, m.inviterUserId)).flatten + g.creatorUserId }
      users ← Future.sequence(allUserIds map (UserOffice.getApiStruct(_, client.userId, client.authId)))
    } yield (users, groups))
  }
}
