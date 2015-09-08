package im.actor.api

import slick.dbio.{DBIO, DBIOAction}
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._
import scalaz.Scalaz._
import scalaz._

package object rpc extends {

  import slick.dbio.NoStream

  object Implicits extends PeersImplicits with HistoryImplicits

  object CommonErrors {
    val GroupNotFound = RpcError(404, "GROUP_NOT_FOUND", "", false, None)
    val InvalidAccessHash = RpcError(403, "INVALID_ACCESS_HASH", "", false, None)
    val UnsupportedRequest = RpcError(400, "REQUEST_NOT_SUPPORTED", "Operation not supported.", false, None)
    val UserNotAuthorized = RpcError(403, "USER_NOT_AUTHORIZED", "", false, None)
    val UserNotFound = RpcError(404, "USER_NOT_FOUND", "", false, None)
    val UserPhoneNotFound = RpcError(404, "USER_PHONE_NOT_FOUND", "", false, None)

    def forbidden(userMessage: String) = RpcError(403, "FORBIDDEN", userMessage, false, None)
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
  ): DBIOAction[RpcError \/ R, NoStream, Nothing] = {
    authorizedAction.getOrElse(DBIO.successful(-\/(RpcError(403, "USER_NOT_AUTHORIZED", "", false, None))))
  }

  def authorizedClient(clientData: ClientData): Result[AuthorizedClientData] =
    DBIOResult.fromOption(CommonErrors.UserNotFound)(clientData.optUserId.map(id ⇒ AuthorizedClientData(clientData.authId, clientData.sessionId, id)))

  type Result[A] = EitherT[DBIO, RpcError, A]

  object DBIOResult {
    implicit def dbioFunctor(implicit ec: ExecutionContext) = new Functor[DBIO] {
      def map[A, B](fa: DBIO[A])(f: A ⇒ B): DBIO[B] = fa map f
    }

    implicit def dbioMonad(implicit ec: ExecutionContext) = new Monad[DBIO] {
      def point[A](a: ⇒ A) = DBIO.successful(a)

      def bind[A, B](fa: DBIO[A])(f: (A) ⇒ DBIO[B]) = fa flatMap f
    }

    implicit def rpcErrorMonoid = new Monoid[RpcError] {
      override def zero: RpcError = throw new Exception()
      override def append(f1: RpcError, f2: ⇒ RpcError): RpcError = throw new Exception()
    }

    def point[A](a: A): Result[A] = EitherT[DBIO, RpcError, A](DBIO.successful(a.right))

    def fromDBIO[A](fa: DBIO[A])(implicit ec: ExecutionContext): Result[A] = EitherT[DBIO, RpcError, A](fa.map(_.right))

    def fromEither[A](va: RpcError \/ A): Result[A] = EitherT[DBIO, RpcError, A](DBIO.successful(va))

    def fromEither[A, B](failure: B ⇒ RpcError)(va: B \/ A): Result[A] = EitherT[DBIO, RpcError, A](DBIO.successful(va.leftMap(failure)))

    def fromOption[A](failure: RpcError)(oa: Option[A]): Result[A] = EitherT[DBIO, RpcError, A](DBIO.successful(oa \/> failure))

    def fromDBIOOption[A](failure: RpcError)(foa: DBIO[Option[A]])(implicit ec: ExecutionContext): Result[A] =
      EitherT[DBIO, RpcError, A](foa.map(_ \/> failure))

    def fromDBIOBoolean(failure: RpcError)(foa: DBIO[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
      EitherT[DBIO, RpcError, Unit](foa.map(r ⇒ if (r) ().right else failure.left))

    def fromDBIOEither[A, B](failure: B ⇒ RpcError)(fva: DBIO[B \/ A])(implicit ec: ExecutionContext): Result[A] =
      EitherT[DBIO, RpcError, A](fva.map(_.leftMap(failure)))

    def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = EitherT[DBIO, RpcError, A](DBIO.from(fu.map(_.right)))

    def fromFutureOption[A](failure: RpcError)(fu: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] = EitherT[DBIO, RpcError, A](DBIO.from(fu.map(_ \/> failure)))

    def fromBoolean[A](failure: RpcError)(oa: Boolean): Result[Unit] = EitherT[DBIO, RpcError, Unit](DBIO.successful(if (oa) ().right else failure.left))
  }

  def constructResult(result: Result[RpcResult])(implicit ec: ExecutionContext): DBIO[RpcResult] =
    result.run.map { _.fold(identity, identity) }
}
