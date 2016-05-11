package im.actor.api

import cats.data.Xor
import im.actor.server.CommonErrors
import im.actor.server.group.GroupErrors.GroupNotFound
import im.actor.server.office.EntityNotFoundError
import im.actor.server.user.UserErrors.UserNotFound

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

package object rpc extends PeersImplicits with HistoryImplicits with DialogConverters {

  object Implicits extends PeersImplicits with HistoryImplicits

  object CommonRpcErrors {
    val GroupNotFound = RpcError(404, "GROUP_NOT_FOUND", "", false, None)
    val InvalidAccessHash = RpcError(403, "INVALID_ACCESS_HASH", "", false, None)
    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val UserNotAuthorized = RpcError(403, "USER_NOT_AUTHORIZED", "", false, None)
    val UserNotFound = RpcError(404, "USER_NOT_FOUND", "", false, None)
    val UserPhoneNotFound = RpcError(404, "USER_PHONE_NOT_FOUND", "", false, None)
    val EntityNotFound = RpcError(404, "ENTITY_NOT_FOUND", "", false, None)
    val NotSupportedInOss = RpcError(400, "NOT_SUPPORTED_IN_OSS", "Feature is not supported in the Open-Source version.", canTryAgain = false, None)
    val IntenalError = RpcError(500, "INTERNAL_ERROR", "", false, None)

    def forbidden(userMessage: String = "You are not allowed to do this.") = RpcError(403, "FORBIDDEN", userMessage, false, None)
  }

  def recoverCommon: PartialFunction[Throwable, RpcError] = {
    case UserNotFound(_)                 ⇒ CommonRpcErrors.UserNotFound
    case GroupNotFound(_)                ⇒ CommonRpcErrors.GroupNotFound
    case EntityNotFoundError             ⇒ CommonRpcErrors.EntityNotFound
    case CommonErrors.Forbidden(message) ⇒ CommonRpcErrors.forbidden(message)
  }

  type OkResp[+A] = A

  object Error {
    def apply[A](e: RpcError)(implicit ev: A <:< RpcResponse): RpcError Xor A = Xor.left(e)

    def unapply(v: RpcError Xor _) =
      v match {
        case Xor.Left(e) ⇒ Some(e)
        case _           ⇒ None
      }
  }

  object Ok {
    def apply[A](rsp: A)(implicit ev: A <:< RpcResponse): RpcError Xor A =
      Xor.Right(rsp)

    def unapply[T <: OkResp[RpcResponse]](v: _ Xor T)(implicit m: ClassTag[T]) =
      v match {
        case Xor.Right(t) ⇒ Some(t)
        case Xor.Left(_)  ⇒ None
      }
  }

  def authorized[R](clientData: ClientData)(fa: AuthorizedClientData ⇒ Future[RpcError Xor R])(implicit ec: ExecutionContext): Future[RpcError Xor R] = {
    toResult(requireAuth(clientData) map fa)
  }

  private def requireAuth(implicit clientData: ClientData): MaybeAuthorized[AuthorizedClientData] =
    clientData.authData match {
      case Some(AuthData(userId, authSid, appId)) ⇒ Authorized(AuthorizedClientData(clientData.authId, clientData.sessionId, userId, authSid, appId, clientData.remoteAddr))
      case None                                   ⇒ NotAuthorized
    }

  private def toResult[R](authorizedFuture: MaybeAuthorized[Future[RpcError Xor R]])(implicit ec: ExecutionContext): Future[RpcError Xor R] =
    recover(authorizedFuture.getOrElse(Future.successful(Error(CommonRpcErrors.UserNotAuthorized))))

  private def recover[A](f: Future[RpcError Xor A])(implicit ec: ExecutionContext): Future[RpcError Xor A] = f recover (recoverCommon andThen { e ⇒ Error(e) })

}
