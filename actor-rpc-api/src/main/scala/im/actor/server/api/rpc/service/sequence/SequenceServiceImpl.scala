package im.actor.server.api.rpc.service.sequence

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.{ActorRef, ActorSystem}
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ResponseSeq, ResponseVoid}
import im.actor.api.rpc.peers.{GroupOutPeer, UserOutPeer}
import im.actor.api.rpc.sequence.{DifferenceUpdate, ResponseGetDifference, SequenceService}
import im.actor.server.models
import im.actor.server.push.SeqUpdatesManager

class SequenceServiceImpl(seqUpdManagerRegion: ActorRef)(implicit db: Database, actorSystem: ActorSystem) extends SequenceService {

  import SeqUpdatesManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetState(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      for {
        seqstate <- getSeqState(seqUpdManagerRegion, client.authId)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction))
  }


  override def jhandleGetDifference(seq: Int, state: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      for {
        seqstate <- getSeqState(seqUpdManagerRegion, client.authId)
        diff <- getDifference(client.authId, state)
      } yield {
        val (updates, needMore) = diff

        val (diffUpdates, userIds, groupIds) = extractDiff(updates)

        // TODO: get users, groups and group members

        Ok(ResponseGetDifference(
          seq = seq,
          state = state,
          updates = diffUpdates,
          needMore = needMore,
          users = Vector.empty,
          groups = Vector.empty,
          phones = Vector.empty,
          emails = Vector.empty))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSubscribeToOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = throw new Exception("Not implemented")

  override def jhandleSubscribeFromOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = throw new Exception("Not implemented")

  override def jhandleSubscribeFromGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = throw new Exception("Not implemented")

  override def jhandleSubscribeToGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = throw new Exception("Not implemented")

  private def extractDiff(updates: Seq[models.sequence.SeqUpdate]): (Vector[DifferenceUpdate], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[DifferenceUpdate], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) =>
        (updates :+ DifferenceUpdate(update.header, update.serializedData),
          userIds ++ update.userIds,
          groupIds ++ update.groupIds)
    }
  }
}
