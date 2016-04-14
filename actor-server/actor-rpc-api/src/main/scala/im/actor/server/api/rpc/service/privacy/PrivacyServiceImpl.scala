package im.actor.server.api.rpc.service.privacy

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.privacy.{ PrivacyService, ResponseLoadBlockedUsers, UpdateUserBlocked, UpdateUserUnblocked }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.model.contact.ContactStatus
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.sequence.SeqUpdatesExtension

import scala.concurrent.{ ExecutionContext, Future }

private object PrivacyServiceErrors {
  val UserAlreadyBlocked = RpcError(400, "USER_ALREADY_BLOCKED", "User is already blocked.", false, None)
  val UserNotBlocked = RpcError(400, "USER_NOT_BLOCKED", "User is not blocked.", false, None)
  val UserNotFound = RpcError(404, "NOT_FOUND", "User is not in contacts.", false, None)
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
        contact ← fromFutureOption(UserNotFound)(db.run(UserContactRepo.find(client.userId, peer.userId)))
        _ ← fromBoolean(UserAlreadyBlocked)(contact.status != ContactStatus.Blocked)
        _ ← fromFuture(db.run(UserContactRepo.block(client.userId, peer.userId)))
        s ← fromFuture(seqUpdExt.deliverSingleUpdate(client.userId, UpdateUserBlocked(peer.userId)))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }

  protected def doHandleUnblockUser(peer: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      (for {
        contact ← fromFutureOption(UserNotFound)(db.run(UserContactRepo.find(client.userId, peer.userId)))
        _ ← fromBoolean(UserNotBlocked)(contact.status == ContactStatus.Blocked)
        _ ← fromFuture(db.run(UserContactRepo.unblock(client.userId, peer.userId)))
        s ← fromFuture(seqUpdExt.deliverSingleUpdate(client.userId, UpdateUserUnblocked(peer.userId)))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }

  protected def doHandleLoadBlockedUsers(clientData: ClientData): Future[HandlerResult[ResponseLoadBlockedUsers]] =
    authorized(clientData) { client ⇒
      for {
        ids ← db.run(UserContactRepo.findBlockedIds(client.userId))
        outPeers ← Future.sequence(ids map (id ⇒ ACLUtils.getUserOutPeer(id, client.authId)))
      } yield Ok(ResponseLoadBlockedUsers(outPeers.toVector))
    }

}