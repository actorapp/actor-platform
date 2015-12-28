package im.actor.api.rpc

import cats.std.{ EitherInstances, FutureInstances }

import scala.concurrent.{ ExecutionContext, Future }
import cats._, cats.data.{ XorT, Xor }, data.Xor._, syntax.all._

import scala.language.implicitConversions
import scalaz.{ \/-, -\/, \/ }

object FutureResultRpcCats extends FutureInstances with EitherInstances {
  type Result[A] = XorT[Future, RpcError, A]
  def Result[A] = XorT.apply[Future, RpcError, A] _

  def point[A](a: A): Result[A] = Result[A](Future.successful(right(a)))

  def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fu.map(right))

  def fromEither[A](va: RpcError Xor A): Result[A] = Result[A](Future.successful(va))

  def fromEither[A, B](failure: B ⇒ RpcError)(va: B Xor A): Result[A] = Result[A](Future.successful(va.leftMap(failure)))

  def fromOption[A](failure: RpcError)(oa: Option[A]): Result[A] = Result[A](Future.successful(oa toRightXor failure))

  def fromFutureOption[A](failure: RpcError)(foa: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_ toRightXor failure))

  def fromFutureBoolean(failure: RpcError)(foa: Future[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) right(()) else left(failure)))

  def fromFutureEither[A, B](failure: B ⇒ RpcError)(fva: Future[B Xor A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fva.map(_.leftMap(failure)))

  def fromFutureEither[A](fva: Future[RpcError Xor A])(implicit ec: ExecutionContext): Result[A] = Result[A](fva)

  def fromBoolean[A](failure: RpcError)(oa: Boolean): Result[Unit] = Result[Unit](Future.successful(if (oa) right(()) else left(failure)))

  implicit class ToScalaz[A](catsResult: RpcError Xor A) {
    def toScalaz: RpcError \/ A = catsResult.fold(-\/(_), \/-(_))
  }
}