package im.actor.api

import scala.reflect._

import scalaz._, std.either._, syntax.monad._
import slick.dbio.{ DBIO, DBIOAction }

import im.actor.api.rpc._

package object rpc {

  import slick.dbio.Effect
  import slick.dbio.NoStream

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

  sealed trait MaybeAuthorized[+A] {
    private def flatten[B](xs: MaybeAuthorized[MaybeAuthorized[B]]): MaybeAuthorized[B] =
      xs match {
        case Authorized(Authorized(x)) => Authorized(x)
        case Authorized(NotAuthorized) => NotAuthorized
        case NotAuthorized             => NotAuthorized
      }

    def flatMap[B](f: A => MaybeAuthorized[B]): MaybeAuthorized[B] = flatten(map(f))

    def map[B](f: A => B): MaybeAuthorized[B] =
      this match {
        case Authorized(a) => Authorized(f(a))
        case NotAuthorized => NotAuthorized
      }

    def getOrElse[B >: A](default: => B): B =
      this match {
        case Authorized(a) => a
        case NotAuthorized => default
      }
  }
  final case class Authorized[+A](a: A) extends MaybeAuthorized[A]
  final case object NotAuthorized extends MaybeAuthorized[Nothing]

  case object MaybeAuthorized extends MaybeAuthorizedInstances

  trait MaybeAuthorizedInstances {
    implicit val maybeAuthorizedInstance = new Functor[MaybeAuthorized] with Monad[MaybeAuthorized] {
      def point[A](a: => A): MaybeAuthorized[A] = Authorized(a)

      def bind[A, B](fa: MaybeAuthorized[A])(f: A => MaybeAuthorized[B]): MaybeAuthorized[B] = fa.flatMap(f)

      override def map[A, B](fa: MaybeAuthorized[A])(f: A => B): MaybeAuthorized[B] = fa.map(f)
    }
  }

  def requireAuth(implicit clientData: ClientData): MaybeAuthorized[Int] =
    clientData.optUserId match {
      case Some(userId) => Authorized(userId)
      case None         => NotAuthorized
    }

  def toDBIOAction[R](
    authorizedAction: MaybeAuthorized[DBIOAction[RpcError \/ R, NoStream, Nothing]]
  ): DBIOAction[RpcError \/ R, NoStream, Nothing] =
    authorizedAction.getOrElse(DBIO.successful(-\/(RpcError(403, "USER_NOT_AUTHORIZED", "", false, None))))
}
