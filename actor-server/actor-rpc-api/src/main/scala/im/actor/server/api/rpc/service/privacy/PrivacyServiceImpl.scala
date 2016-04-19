package im.actor.server.api.rpc.service.privacy

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.privacy.{ PrivacyService, ResponseLoadBlockedUsers, UpdateUserBlocked, UpdateUserUnblocked }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.model.social.{ Relation, RelationStatus }
import im.actor.server.persist.social.RelationRepo
import im.actor.server.sequence.SeqUpdatesExtension

import scala.concurrent.{ ExecutionContext, Future }

private object PrivacyServiceErrors {
  val UserAlreadyBlocked = RpcError(400, "USER_ALREADY_BLOCKED", "User is already blocked.", false, None)
  val UserNotBlocked = RpcError(400, "USER_NOT_BLOCKED", "User is not blocked.", false, None)
}

final class PrivacyServiceImpl(implicit system: ActorSystem) extends PrivacyService {
  import FutureResultRpc._
  import PrivacyServiceErrors._

  implicit protected val ec: ExecutionContext = system.dispatcher

  private val db = DbExtension(system).db
  private val seqUpdExt = SeqUpdatesExtension(system)

  protected def doHandleBlockUser(peer: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      (for {
        optRelation ← fromFuture(db.run(RelationRepo.find(client.userId, peer.userId)))
        _ ← optRelation match {
          case Some(relation) ⇒
            for {
              _ ← fromBoolean(UserAlreadyBlocked)(relation.status != RelationStatus.Blocked)
              _ ← fromFuture(db.run(RelationRepo.block(client.userId, peer.userId)))
            } yield ()
          case None ⇒
            val newRelation = Relation(client.userId, peer.userId, RelationStatus.Blocked)
            fromFuture(db.run(RelationRepo.create(newRelation)))
        }
        s ← fromFuture(seqUpdExt.deliverSingleUpdate(client.userId, UpdateUserBlocked(peer.userId)))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }

  protected def doHandleUnblockUser(peer: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      (for {
        optRelation ← fromFuture(db.run(RelationRepo.find(client.userId, peer.userId)))
        _ ← optRelation match {
          case Some(relation) ⇒
            for {
              _ ← fromBoolean(UserNotBlocked)(relation.status == RelationStatus.Blocked)
              _ ← fromFuture(db.run(RelationRepo.unblock(client.userId, peer.userId)))
            } yield ()
          case None ⇒
            val newRelation = Relation(client.userId, peer.userId, RelationStatus.Approved)
            fromFuture(db.run(RelationRepo.create(newRelation)))
        }
        s ← fromFuture(seqUpdExt.deliverSingleUpdate(client.userId, UpdateUserUnblocked(peer.userId)))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }

  protected def doHandleLoadBlockedUsers(clientData: ClientData): Future[HandlerResult[ResponseLoadBlockedUsers]] =
    authorized(clientData) { client ⇒
      for {
        ids ← db.run(RelationRepo.fetchBlockedIds(client.userId))
        outPeers ← Future.sequence(ids map (id ⇒ ACLUtils.getUserOutPeer(id, client.authId)))
      } yield Ok(ResponseLoadBlockedUsers(outPeers.toVector))
    }

}