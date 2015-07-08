package im.actor.server.api.rpc.service.sequence

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Source }
import akka.stream.stage._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.api.rpc.sequence.{ DifferenceUpdate, ResponseGetDifference, SequenceService }
import im.actor.server.models.sequence.SeqUpdate
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.session.{ SessionMessage, SessionRegion }
import im.actor.server.util.{ AnyRefLogSource, GroupUtils, UserUtils }
import im.actor.server.{ models, persist }

class SequenceServiceImpl(config: SequenceServiceConfig)(
  implicit
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  sessionRegion:       SessionRegion,
  db:                  Database,
  actorSystem:         ActorSystem,
  materializer:        Materializer
) extends SequenceService {
  import AnyRefLogSource._
  import GroupUtils._
  import SeqUpdatesManager._
  import UserUtils._

  private val log = Logging(actorSystem, this)

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val maxUpdateSizeInBytes: Long = config.maxUpdateSizeInBytes

  override def jhandleGetState(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        seqstate ← getSeqState(client.authId)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  case class UpdateResult(update: DifferenceUpdate, userIds: Set[Int], groupIds: Set[Int])
  case class FinalState(needMore: Boolean, seq: Int, state: Array[Byte])

  private def updateSizeBounded(maxSizeInBytes: Long, defaultState: FinalState): Flow[SeqUpdate, Either[FinalState, UpdateResult], Unit] = Flow[SeqUpdate].transform {
    () ⇒
      new PushPullStage[SeqUpdate, Either[FinalState, UpdateResult]] {
        var finalState: FinalState = defaultState
        var sizeInBytes: Long = 0

        override def onUpstreamFinish(ctx: Context[Either[FinalState, UpdateResult]]): TerminationDirective =
          ctx.absorbTermination()

        override def onPush(elem: SeqUpdate, ctx: Context[Either[FinalState, UpdateResult]]) = {
          val update = DifferenceUpdate(elem.header, elem.serializedData)
          sizeInBytes += update.toByteArray.length

          if (sizeInBytes > maxSizeInBytes) {
            ctx.pushAndFinish(Left(finalState))
          } else {
            finalState = FinalState(needMore = true, elem.seq, timestampToBytes(elem.timestamp))
            ctx.push(Right(UpdateResult(update, elem.userIds, elem.groupIds)))
          }
        }

        override def onPull(ctx: Context[Either[FinalState, UpdateResult]]): SyncDirective =
          if (!ctx.isFinishing) {
            ctx.pull()
          } else {
            ctx.pushAndFinish(Left(finalState.copy(needMore = false)))
          }
      }
  }

  override def jhandleGetDifference(seq: Int, state: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetDifference]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val defaultState = FinalState(needMore = false, seq, state)
      val updateStream = db.stream(persist.sequence.SeqUpdate.findAfter(client.authId, bytesToTimestamp(state)))
      val result = Source(updateStream)
        .via(updateSizeBounded(maxUpdateSizeInBytes, defaultState))
        .runFold((Vector.empty[DifferenceUpdate], Set.empty[Int], Set.empty[Int], defaultState)) {
          case ((updates, accUserIds, accGroupIds, finalState), el) ⇒
            el match {
              case Right(UpdateResult(update, userIds, groupIds)) ⇒ (updates :+ update, accUserIds ++ userIds, accGroupIds ++ groupIds, finalState)
              case Left(newState: FinalState)                     ⇒ (updates, accUserIds, accGroupIds, newState)
            }
        }
      for {
        // FIXME: would new updates between getSeqState and getDifference break client state?
        (diffUpdates, userIds, groupIds, newState) ← DBIO.from(result)
        (users, groups) ← getUsersGroups(userIds, groupIds)
      } yield {
        log.debug("Requested timestamp {}, {}", bytesToTimestamp(state), clientData)
        //        log.debug("Updates {}, {}", updates, clientData)
        log.debug("New state {}, {}", bytesToTimestamp(newState.state), clientData)

        Ok(ResponseGetDifference(
          seq = newState.seq,
          state = newState.state,
          updates = diffUpdates,
          needMore = newState.needMore,
          users = users.toVector,
          groups = groups.toVector
        ))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleSubscribeToOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction)) andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionMessage.envelope(
          clientData.authId,
          clientData.sessionId,
          SessionMessage.SubscribeToOnline(userIds)
        )
    }
  }

  override def jhandleSubscribeFromOnline(users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction)) andThen {
      case Success(_) ⇒
        // FIXME: #security check access hashes
        val userIds = users.map(_.userId).toSet

        sessionRegion.ref ! SessionMessage.envelope(
          clientData.authId,
          clientData.sessionId,
          SessionMessage.SubscribeFromOnline(userIds)
        )
    }
  }

  override def jhandleSubscribeFromGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionMessage.envelope(clientData.authId, clientData.sessionId, SessionMessage.SubscribeFromGroupOnline(groups.map(_.groupId).toSet))
    }
  }

  override def jhandleSubscribeToGroupOnline(groups: Vector[GroupOutPeer], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future.successful(Ok(ResponseVoid)) andThen {
      case _ ⇒
        // FIXME: #security check access hashes
        sessionRegion.ref ! SessionMessage.envelope(clientData.authId, clientData.sessionId, SessionMessage.SubscribeToGroupOnline(groups.map(_.groupId).toSet))
    }
  }

  private def extractDiff(updates: Seq[models.sequence.SeqUpdate]): (Vector[DifferenceUpdate], Set[Int], Set[Int]) = {
    updates.foldLeft[(Vector[DifferenceUpdate], Set[Int], Set[Int])](Vector.empty, Set.empty, Set.empty) {
      case ((updates, userIds, groupIds), update) ⇒
        (updates :+ DifferenceUpdate(update.header, update.serializedData),
          userIds ++ update.userIds,
          groupIds ++ update.groupIds)
    }
  }

  private def getUsersGroups(userIds: Set[Int], groupIds: Set[Int])(implicit client: AuthorizedClientData) = {
    for {
      groups ← getGroupsStructs(groupIds)
      // TODO: #perf optimize collection operations
      allUserIds = userIds ++ groups.foldLeft(Set.empty[Int]) { (ids, g) ⇒ ids ++ g.members.map(m ⇒ Seq(m.userId, m.inviterUserId)).flatten + g.creatorUserId }
      users ← getUserStructsPar(allUserIds)
    } yield (users, groups)
  }
}
