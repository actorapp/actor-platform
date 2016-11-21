package im.actor.concurrent

import akka.http.scaladsl.util.FastFuture
import cats.data.Xor._
import cats.data.{ Xor, XorT }
import cats.instances.{ EitherInstances, FutureInstances }
import cats.syntax.all._

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

trait FutureResult[ErrorCase] extends FutureInstances with EitherInstances {
  type Result[A] = XorT[Future, ErrorCase, A]
  def Result[A] = XorT.apply[Future, ErrorCase, A] _

  def point[A](a: A): Result[A] = Result[A](FastFuture.successful(a.right))

  def fromXor[A](va: ErrorCase Xor A): Result[A] = Result[A](FastFuture.successful(va))

  def fromXor[A, B](errorHandle: B ⇒ ErrorCase)(va: B Xor A): Result[A] = Result[A](FastFuture.successful(va leftMap errorHandle))

  def fromOption[A](failure: ErrorCase)(oa: Option[A]): Result[A] = Result[A](FastFuture.successful(oa toRightXor failure))

  def fromBoolean[A](failure: ErrorCase)(oa: Boolean): Result[Unit] = Result[Unit](FastFuture.successful(if (oa) ().right else failure.left))

  def fromFuture[A](fa: Future[A])(implicit ec: ExecutionContext): Result[A] = Result[A](fa map right)

  def fromFuture[A](errorHandle: Throwable ⇒ ErrorCase)(fu: Future[A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fu.map(_.right) recover { case e ⇒ errorHandle(e).left })

  def fromFutureXor[A](fva: Future[ErrorCase Xor A])(implicit ec: ExecutionContext): Result[A] = Result[A](fva)

  def fromFutureXor[A](errorHandle: Throwable ⇒ ErrorCase)(fea: Future[Throwable Xor A])(implicit ec: ExecutionContext): Result[A] =
    Result[A](fea map (either ⇒ either.leftMap(errorHandle)))

  def fromFutureOption[A](failure: ErrorCase)(foa: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] =
    Result[A](foa.map(_ toRightXor failure))

  def fromFutureBoolean(failure: ErrorCase)(foa: Future[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
    Result[Unit](foa.map(r ⇒ if (r) ().right else failure.left))
}

// TODO: find right place for it
trait XorEitherConversions {
  implicit def toEither[E, A](xor: E Xor A): Either[E, A] = xor.toEither

  implicit def toEither[E, A](fxor: Future[E Xor A])(implicit ec: ExecutionContext): Future[Either[E, A]] = fxor map (_.toEither)
}
