package im.actor.concurrent

import cats.data.{ Xor, XorT }

import scala.concurrent.{ ExecutionContext, Future }

trait FutureResultCats[ErrorCase] {
  type Result[A] = XorT[Future, ErrorCase, A]

  implicit def futureFunctor(implicit ec: ExecutionContext): cats.Functor[Future] = new cats.Functor[Future] {
    def map[A, B](fa: Future[A])(f: (A) ⇒ B) = fa map f
  }
  implicit def futureMonad(implicit ec: ExecutionContext): cats.Monad[Future] = new cats.Monad[Future] {
    def pure[A](x: A) = Future.successful(x)
    def flatMap[A, B](fa: Future[A])(f: (A) ⇒ Future[B]) = fa flatMap f
  }

  implicit class Boolean2Xor(val self: Boolean) {
    def toXor[A](failure: A): A Xor Unit = if (self) Xor.right(()) else Xor.left(failure)
  }

  def point[A](a: A): Result[A] = XorT[Future, ErrorCase, A](Future.successful(Xor.right(a)))

  def fromFuture[A](fa: Future[A])(implicit ec: ExecutionContext): Result[A] = XorT[Future, ErrorCase, A](fa map Xor.right)

  def fromXor[A](va: ErrorCase Xor A): Result[A] = XorT[Future, ErrorCase, A](Future.successful(va))

  def fromXor[A](errorHandle: Throwable ⇒ ErrorCase)(va: Throwable Xor A): Result[A] = XorT[Future, ErrorCase, A](Future.successful(va leftMap errorHandle))

  def fromOption[A](failure: ErrorCase)(oa: Option[A]): Result[A] = XorT[Future, ErrorCase, A](Future.successful(Xor.fromOption(oa, failure)))

  def fromFutureOption[A](failure: ErrorCase)(foa: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    XorT[Future, ErrorCase, A](foa map (Xor.fromOption(_, failure)))

  def fromFutureBoolean(failure: ErrorCase)(foa: Future[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    XorT[Future, ErrorCase, Unit](foa map (_.toXor(failure)))

  def fromBoolean(failure: ErrorCase)(oa: Boolean): Result[Unit] = XorT[Future, ErrorCase, Unit](Future.successful(oa.toXor(failure)))
}
