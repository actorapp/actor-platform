package im.actor.api

import scala.reflect._

import scalaz._, std.either._
import slick.dbio.{ DBIO, DBIOAction }

package object rpc extends {
  import slick.dbio.Effect
  import slick.dbio.NoStream

  object Implicits extends PeersImplicits with MessagingImplicits

  object CommonErrors {
    val InvalidAccessHash = RpcError(403, "INVALID_ACCESS_HASH", "", false, None)
    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val UserNotAuthorized = RpcError(403, "USER_NOT_AUTHORIZED", "", false, None)
    val UserNotFound = RpcError(404, "USER_NOT_FOUND", "", false, None)
    val UserPhoneNotFound = RpcError(404, "USER_PHONE_NOT_FOUND", "", false, None)
    val Internal = RpcError(500, "INTERNAL_SERVER_ERROR", "", false, None)
  }

  type OkResp[+A] = A

  object Error {
    def apply(e: RpcError) = -\/(e)
    def unapply(v: RpcError \/ _) =
      v match {
        case \/-(_) => None
        case -\/(e) => Some(e)
      }
  }

  object Ok {
    def apply[A](rsp: A)(implicit ev: A <:< RpcResponse): RpcError \/ A =
      \/-(rsp)

    def unapply[T <: OkResp[RpcResponse]](v: _ \/ T)(implicit m: ClassTag[T]) =
      v match {
        case \/-(t) => Some(t)
        case -\/(_) => None
      }
  }

  def requireAuth(implicit clientData: ClientData): MaybeAuthorized[AuthorizedClientData] =
    clientData.optUserId match {
      case Some(userId) => Authorized(AuthorizedClientData(clientData.authId, userId))
      case None         => NotAuthorized
    }

  def toDBIOAction[R](
    authorizedAction: MaybeAuthorized[DBIOAction[RpcError \/ R, NoStream, Nothing]]
  ): DBIOAction[RpcError \/ R, NoStream, Nothing] =
    authorizedAction.getOrElse(DBIO.successful(-\/(RpcError(403, "USER_NOT_AUTHORIZED", "", false, None))))
}
