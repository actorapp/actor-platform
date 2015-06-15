package im.actor.api

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect._

import scalaz._, Scalaz._
import slick.dbio.{ DBIO, DBIOAction, Effect }
import slick.driver.PostgresDriver.api._

package object rpc extends {
  import slick.dbio.NoStream

  object Implicits extends PeersImplicits with GroupsImplicits with HistoryImplicits

  object CommonErrors {
    val GroupNotFound = RpcError(404, "GROUP_NOT_FOUND", "", false, None)
    val InvalidAccessHash = RpcError(403, "INVALID_ACCESS_HASH", "", false, None)
    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val UserNotAuthorized = RpcError(403, "USER_NOT_AUTHORIZED", "", false, None)
    val UserNotFound = RpcError(404, "USER_NOT_FOUND", "", false, None)
    val UserPhoneNotFound = RpcError(404, "USER_PHONE_NOT_FOUND", "", false, None)
    def noPermission(userMessage: String) = RpcError(403, "NO_PERMISSION", userMessage, false, None)
  }

  type OkResp[+A] = A

  object Error {
    def apply[A](e: RpcError)(implicit ev: A <:< RpcResponse): RpcError \/ A =
      -\/(e)
    def unapply(v: RpcError \/ _) =
      v match {
        case -\/(e) ⇒ Some(e)
        case _      ⇒ None
      }
  }

  object Ok {
    def apply[A](rsp: A)(implicit ev: A <:< RpcResponse): RpcError \/ A =
      \/-(rsp)

    def unapply[T <: OkResp[RpcResponse]](v: _ \/ T)(implicit m: ClassTag[T]) =
      v match {
        case \/-(t) ⇒ Some(t)
        case -\/(_) ⇒ None
      }
  }

  def authorizedAction[R](clientData: ClientData)(f: AuthorizedClientData ⇒ DBIOAction[RpcError \/ R, NoStream, Nothing])(implicit db: Database): Future[RpcError \/ R] = {
    val authorizedAction = requireAuth(clientData).map(f)
    db.run(toDBIOAction(authorizedAction))
  }

  def requireAuth(implicit clientData: ClientData): MaybeAuthorized[AuthorizedClientData] =
    clientData.optUserId match {
      case Some(userId) ⇒ Authorized(AuthorizedClientData(clientData.authId, clientData.sessionId, userId))
      case None         ⇒ NotAuthorized
    }

  def toDBIOAction[R](
    authorizedAction: MaybeAuthorized[DBIOAction[RpcError \/ R, NoStream, Nothing]]
  ): DBIOAction[RpcError \/ R, NoStream, Nothing] =
    authorizedAction.getOrElse(DBIO.successful(-\/(RpcError(403, "USER_NOT_AUTHORIZED", "", false, None))))

  type SimpleDBIOAction[A] = DBIOAction[A, NoStream, Effect]

  type DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A]

  object DBIOResult {
    def point[A](a: A): DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A](DBIO.successful(a.right))
    def fromDBIO[A](fa: SimpleDBIOAction[A])(implicit ec: ExecutionContext): DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A](fa.map(_.right))
    def fromEither[A](va: RpcResult \/ A): DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A](DBIO.successful(va))
    def fromEither[A, B](failure: B ⇒ RpcResult)(va: B \/ A): DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A](DBIO.successful(va.leftMap(failure)))
    def fromOption[A](failure: RpcResult)(oa: Option[A]): DBIORpcResult[A] = EitherT[SimpleDBIOAction, RpcResult, A](DBIO.successful(oa \/> failure))
    def fromDBIOOption[A](failure: RpcResult)(foa: SimpleDBIOAction[Option[A]])(implicit ec: ExecutionContext): DBIORpcResult[A] =
      EitherT[SimpleDBIOAction, RpcResult, A](foa.map(_ \/> failure))
    def fromDBIOEither[A, B](failure: B ⇒ RpcResult)(fva: SimpleDBIOAction[B \/ A])(implicit ec: ExecutionContext): DBIORpcResult[A] =
      EitherT[SimpleDBIOAction, RpcResult, A](fva.map(_.leftMap(failure)))
  }

  def constructResult(result: DBIORpcResult[RpcResult])(implicit ec: ExecutionContext): DBIOAction[RpcResult, NoStream, Effect] =
    result.run.map { _.fold(identity, identity) }
}
