package im.actor.api.rpc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import im.actor.api.rpc.CommonRpcErrors.InvalidAccessHash
import im.actor.api.rpc.peers._
import im.actor.server.acl.ACLUtils._
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupErrors.{ GroupAlreadyDeleted, GroupNotFound }
import im.actor.server.user.UserErrors.UserNotFound
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object PeerHelpers {

  def withOutPeer[R <: RpcResponse](outPeer: ApiOutPeer)(authorizedAction: ⇒ Future[RpcError Xor R])(
    implicit
    client: AuthorizedClientData,
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import system.dispatcher
    accessHashCheck(checkOutPeer(outPeer, client.authId), authorizedAction)
  }

  // single
  def withUserOutPeer[R <: RpcResponse](userOutPeer: ApiUserOutPeer)(authorizedAction: ⇒ Future[RpcError Xor R])(
    implicit
    client: AuthorizedClientData,
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import system.dispatcher
    accessHashCheck(checkUserOutPeer(userOutPeer, client.authId), authorizedAction)
  }

  // single
  def withGroupOutPeer[R <: RpcResponse](groupOutPeer: ApiGroupOutPeer)(authorizedAction: ⇒ Future[RpcError Xor R])(
    implicit
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import system.dispatcher
    accessHashCheck(checkGroupOutPeer(groupOutPeer), authorizedAction)
  }

  // multiple
  def withUserOutPeers[R <: RpcResponse](userOutPeers: Seq[ApiUserOutPeer])(authorizedAction: ⇒ Future[RpcError Xor R])(
    implicit
    client: AuthorizedClientData,
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import system.dispatcher
    accessHashCheck(checkUserOutPeers(userOutPeers, client.authId), authorizedAction)
  }

  // multiple
  def withGroupOutPeers[R <: RpcResponse](groupOutPeers: Seq[ApiGroupOutPeer])(authorizedAction: ⇒ Future[RpcError Xor R])(
    implicit
    system: ActorSystem
  ): Future[RpcError Xor R] = {
    import system.dispatcher
    accessHashCheck(checkGroupOutPeers(groupOutPeers), authorizedAction)
  }

  private def accessHashCheck[R <: RpcResponse](check: Future[Boolean], authorizedAction: ⇒ Future[RpcError Xor R])(implicit ec: ExecutionContext) =
    check flatMap { isValid ⇒
      if (isValid) {
        authorizedAction
      } else {
        FastFuture.successful(Error(InvalidAccessHash))
      }
    } recover {
      case e: Exception ⇒ Error(handleNotFound(e))
    }

  private def handleNotFound: PartialFunction[Throwable, RpcError] = {
    case _: UserNotFound        ⇒ CommonRpcErrors.UserNotFound
    case _: GroupNotFound       ⇒ CommonRpcErrors.GroupNotFound
    case _: GroupAlreadyDeleted ⇒ CommonRpcErrors.GroupDeleted
    case e                      ⇒ throw e
  }
}
