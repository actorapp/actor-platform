package im.actor.api.rpc

import cats.data.Xor._
import cats.data.{ Xor, XorT }
import cats.syntax.all._
import cats.{ Functor, Monad }
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object DBIOResultRpc {

  type Result[A] = XorT[DBIO, RpcError, A]
  def Result[A] = XorT.apply[DBIO, RpcError, A] _

  implicit def dbioFunctor(implicit ec: ExecutionContext) = new Functor[DBIO] with Monad[DBIO] {
    def pure[A](a: A): DBIO[A] = DBIO.successful(a)
    def flatMap[A, B](fa: DBIO[A])(f: A ⇒ DBIO[B]): DBIO[B] = fa flatMap f
    override def map[A, B](fa: DBIO[A])(f: A ⇒ B): DBIO[B] = fa map f
    def tailRecM[A, B](a: A)(f: A ⇒ DBIO[Either[A, B]]): DBIO[B] = defaultTailRecM(a)(f)
  }

  def point[A](a: A): Result[A] = Result[A](DBIO.successful(right(a)))

  def fromDBIO[A](fa: DBIO[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fa map right)

  def fromEither[A](va: RpcError Xor A): Result[A] = Result[A](DBIO.successful(va))

  def fromEither[A, B](failure: B ⇒ RpcError)(va: B Xor A): Result[A] = Result[A](DBIO.successful(va.leftMap(failure)))

  def fromOption[A](failure: RpcError)(oa: Option[A]): Result[A] = Result[A](DBIO.successful(oa toRightXor failure))

  def fromDBIOOption[A](failure: RpcError)(foa: DBIO[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_ toRightXor failure))

  def fromDBIOBoolean(failure: RpcError)(foa: DBIO[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) ().right else failure.left))

  def fromDBIOEither[A, B](failure: B ⇒ RpcError)(fva: DBIO[B Xor A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fva.map(_.leftMap(failure)))

  def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](DBIO.from(fu.map(_.right)))

  def fromFutureOption[A](failure: RpcError)(fu: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](DBIO.from(fu.map(_ toRightXor failure)))

  def fromBoolean[A](failure: RpcError)(oa: Boolean): Result[Unit] =
    Result[Unit](DBIO.successful(if (oa) ().right else failure.left))
}
