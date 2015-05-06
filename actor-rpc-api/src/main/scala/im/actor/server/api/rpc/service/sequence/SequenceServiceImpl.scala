package im.actor.server.api.rpc.service.sequence

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.groups.Group
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.api.rpc.sequence.{ DifferenceUpdate, ResponseGetDifference, SequenceService }
import im.actor.api.rpc.users.{ Phone, User }
import im.actor.server.models
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.session.{ SessionMessage, SessionRegion }
import im.actor.server.util.{ GroupUtils, UserUtils }

class SequenceServiceImpl(
  implicit
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  sessionRegion:       SessionRegion,
  db:                  Database,
  actorSystem:         ActorSystem
) extends SequenceService {
  import GroupUtils._
  import SeqUpdatesManager._
  import UserUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetState(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        seqstate ← getSeqState(client.authId)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetDifference(seq: Int, state: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        // FIXME: would new updates between getSeqState and getDifference break client state?
        seqstate ← getSeqState(client.authId)
        (updates, needMore) ← getDifference(client.authId, state)
        (diffUpdates, userIds, groupIds) = extractDiff(updates)
        (users, phones, groups) ← getUsersPhonesGroups(userIds, groupIds)
      } yield {
        Ok(ResponseGetDifference(
          seq = seqstate._1,
          state = seqstate._2,
          updates = diffUpdates,
          needMore = needMore,
          users = users.toVector,
          groups = groups.toVector,
          phones = phones.toVector,
          emails = Vector.empty
        ))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleSubscribeToOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      // FIXME: #security check access hashes
      val userIds = users.map(_.userId).toSet

      sessionRegion.ref ! SessionMessage.envelope(
        clientData.authId,
        clientData.sessionId,
        SessionMessage.SubscribeToOnline(userIds)
      )

      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSubscribeFromOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      // FIXME: #security check access hashes
      val userIds = users.map(_.userId).toSet

      sessionRegion.ref ! SessionMessage.envelope(
        clientData.authId,
        clientData.sessionId,
        SessionMessage.SubscribeFromOnline(userIds)
      )

      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSubscribeFromGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    // FIXME: #security check access hashes
    sessionRegion.ref ! SessionMessage.envelope(clientData.authId, clientData.sessionId, SessionMessage.SubscribeFromGroupOnline(groups.map(_.groupId).toSet))

    Future.successful(Ok(ResponseVoid))
  }

  override def jhandleSubscribeToGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    // FIXME: #security check access hashes
    sessionRegion.ref ! SessionMessage.envelope(clientData.authId, clientData.sessionId, SessionMessage.SubscribeToGroupOnline(groups.map(_.groupId).toSet))

    Future.successful(Ok(ResponseVoid))
  }

  private def extractDiff(updates: Seq[models.sequence.SeqUpdate]): (Vector[DifferenceUpdate], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[DifferenceUpdate], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) ⇒
        (updates :+ DifferenceUpdate(update.header, update.serializedData),
          userIds ++ update.userIds,
          groupIds ++ update.groupIds)
    }
  }

  private def getUsersPhonesGroups(userIds: Set[Int], groupIds: Set[Int])(implicit client: AuthorizedClientData): dbio.DBIOAction[(Seq[User], Seq[Phone], Seq[Group]), NoStream, Read with Read with Read with Read with Read with Read with Read with Read with Read] = {
    for {
      groups ← getGroupsStructs(groupIds)
      allUserIds = userIds ++ groups.foldLeft(Set.empty[Int]) { (ids, g) ⇒ ids ++ g.members.map(_.userId) }
      users ← userStructs(allUserIds)
      phones ← getUserPhones(allUserIds)
    } yield (users, phones, groups)
  }
}
