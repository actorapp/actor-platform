package im.actor.api.rpc

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Scalaz._
import scalaz._

object FutureResultRpc {
  type Result[A] = EitherT[Future, RpcError, A]

  def Result[A] = EitherT.apply[Future, RpcError, A] _

  implicit def futureFunctor(implicit ec: ExecutionContext) = new Functor[Future] {
    def map[A, B](fa: Future[A])(f: A ⇒ B): Future[B] = fa map f
  }

  implicit def futureMonad(implicit ec: ExecutionContext) = new Monad[Future] {
    def point[A](a: ⇒ A) = Future.successful(a)

    def bind[A, B](fa: Future[A])(f: (A) ⇒ Future[B]) = fa flatMap f
  }

  implicit def rpcErrorMonoid = new Monoid[RpcError] {
    override def zero: RpcError = throw new Exception()

    override def append(f1: RpcError, f2: ⇒ RpcError): RpcError = throw new Exception()
  }

  def point[A](a: A): Result[A] = EitherT[Future, RpcError, A](Future.successful(a.right))

  def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fu.map(_.right))

  def fromFuture[A](failure: Throwable ⇒ RpcError)(fu: Future[A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fu.map(_.right).recover {
      case e ⇒ failure(e).left
    })

  def fromEither[A](va: RpcError \/ A): Result[A] = Result[A](Future.successful(va))

  def fromEither[A, B](failure: B ⇒ RpcError)(va: B \/ A): Result[A] = Result[A](Future.successful(va.leftMap(failure)))

  def fromOption[A](failure: RpcError)(oa: Option[A]): Result[A] = Result[A](Future.successful(oa \/> failure))

  def fromFutureOption[A](failure: RpcError)(foa: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_ \/> failure))

  def fromFutureBoolean(failure: RpcError)(foa: Future[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) ().right else failure.left))

  def fromFutureEither[A, B](failure: B ⇒ RpcError)(fva: Future[B \/ A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fva.map(_.leftMap(failure)))

  def fromBoolean[A](failure: RpcError)(oa: Boolean): Result[Unit] = Result[Unit](Future.successful(if (oa) ().right else failure.left))
}