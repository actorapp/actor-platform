package im.actor.server.api.http

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

import akka.http.scaladsl.model.StatusCode
import cats.data.{ Xor, XorT }
import slick.dbio.DBIO

package object dashboard {
  type ErrorResult = (StatusCode, DashboardError)
  type SuccessResult[A] = (StatusCode, A)
  type Result[A] = XorT[DBIO, ErrorResult, A]

  object DBIOResult {

    implicit def dbioFunctor(implicit ec: ExecutionContext): cats.Functor[DBIO] = new cats.Functor[DBIO] {
      def map[A, B](fa: DBIO[A])(f: (A) ⇒ B) = fa map f
    }
    implicit def dbioMonad(implicit ec: ExecutionContext): cats.Monad[DBIO] = new cats.Monad[DBIO] {
      def pure[A](x: A) = DBIO.successful(x)
      def flatMap[A, B](fa: DBIO[A])(f: (A) ⇒ DBIO[B]) = fa flatMap f
    }

    implicit class Opt2Xor[A](val self: Option[A]) {
      def toXor[B](failure: B): B Xor A = self map Xor.right getOrElse Xor.left(failure)
    }

    implicit class Boolean2Xor(val self: Boolean) {
      def toXor[A](failure: A): A Xor Unit = if (self) Xor.right(()) else Xor.left(failure)
    }

    def point[A](a: A): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.successful(Xor.right(a)))

    def fromDBIO[A](fa: DBIO[A])(implicit ec: ExecutionContext): Result[A] = XorT[DBIO, ErrorResult, A](fa map Xor.right)

    def fromEither[A](va: ErrorResult Xor A): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.successful(va))

    def fromEither[A, B](failure: B ⇒ ErrorResult)(va: B Xor A): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.successful(va leftMap failure))

    def fromOption[A](failure: ErrorResult)(oa: Option[A]): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.successful(oa.toXor(failure)))

    def fromDBIOOption[A](failure: ErrorResult)(foa: DBIO[Option[A]])(implicit ec: ExecutionContext): Result[A] =
      XorT[DBIO, ErrorResult, A](foa map (_.toXor(failure)))

    def fromDBIOBoolean(failure: ErrorResult)(foa: DBIO[Boolean])(implicit ec: ExecutionContext): Result[Unit] =
      XorT[DBIO, ErrorResult, Unit](foa map (_.toXor(failure)))

    def fromDBIOEither[A, B](failure: B ⇒ ErrorResult)(fva: DBIO[B Xor A])(implicit ec: ExecutionContext): Result[A] =
      XorT[DBIO, ErrorResult, A](fva map (_.leftMap(failure)))

    def fromFuture[A](fu: Future[A])(implicit ec: ExecutionContext): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.from(fu map Xor.right))

    def fromFutureOption[A](failure: ErrorResult)(fu: Future[Option[A]])(implicit ec: ExecutionContext): Result[A] = XorT[DBIO, ErrorResult, A](DBIO.from(fu map (_.toXor(failure))))

    def fromBoolean(failure: ErrorResult)(oa: Boolean): Result[Unit] = XorT[DBIO, ErrorResult, Unit](DBIO.successful(oa.toXor(failure)))
  }
}