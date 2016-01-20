package im.actor.cats

import cats._
import cats.data.Xor
import cats.data.Xor._
import cats.syntax.all._
import slick.dbio.{ DBIO, FailureAction, SuccessAction }

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object dbio extends DBIOInstances

trait DBIOInstances extends DBIOInstances1 {

  implicit def DBIOInstance(implicit ec: ExecutionContext): MonadError[DBIO, Throwable] with CoflatMap[DBIO] =
    new DBIOCoflatMap with MonadError[DBIO, Throwable] {
      def pure[A](x: A): DBIO[A] = DBIO.successful(x)

      override def pureEval[A](x: Eval[A]): DBIO[A] = DBIO.successful(x.value)

      def flatMap[A, B](fa: DBIO[A])(f: A ⇒ DBIO[B]): DBIO[B] = fa.flatMap(f)

      def handleErrorWith[A](fea: DBIO[A])(f: Throwable ⇒ DBIO[A]): DBIO[A] =
        fea flatMap {
          case succ: SuccessAction[_] ⇒ succ.asInstanceOf[SuccessAction[A]]
          case FailureAction(t)       ⇒ f(t)
        }

      def raiseError[A](e: Throwable): DBIO[A] = DBIO.failed(e)
      override def handleError[A](fea: DBIO[A])(f: Throwable ⇒ A): DBIO[A] =
        fea map {
          case SuccessAction(res) ⇒ res.asInstanceOf[A]
          case FailureAction(t)   ⇒ f(t)
        }

      override def attempt[A](fa: DBIO[A]): DBIO[Throwable Xor A] =
        fa map {
          case SuccessAction(res) ⇒ right(res.asInstanceOf[A])
          case FailureAction(t)   ⇒ left(t)
        }

      override def recover[A](fa: DBIO[A])(pf: PartialFunction[Throwable, A]): DBIO[A] =
        fa map {
          case succ: SuccessAction[_] ⇒ succ.asInstanceOf[A]
          case FailureAction(t)       ⇒ pf(t)
        }

      override def recoverWith[A](fa: DBIO[A])(pf: PartialFunction[Throwable, DBIO[A]]): DBIO[A] =
        fa flatMap {
          case succ: SuccessAction[_] ⇒ succ.asInstanceOf[SuccessAction[A]]
          case FailureAction(t)       ⇒ pf(t)
        }

      override def map[A, B](fa: DBIO[A])(f: A ⇒ B): DBIO[B] = fa.map(f)
    }

  implicit def DBIOGroup[A: Group](implicit ec: ExecutionContext): Group[DBIO[A]] =
    new DBIOGroup[A]
}

private[cats] sealed trait DBIOInstances1 extends DBIOInstances2 {
  implicit def DBIOMonoid[A: Monoid](implicit ec: ExecutionContext): Monoid[DBIO[A]] =
    new DBIOMonoid[A]
}

private[cats] sealed trait DBIOInstances2 {
  implicit def DBIOSemigroup[A: Semigroup](implicit ec: ExecutionContext): Semigroup[DBIO[A]] =
    new DBIOSemigroup[A]
}

private[cats] abstract class DBIOCoflatMap(implicit ec: ExecutionContext) extends CoflatMap[DBIO] {
  def map[A, B](fa: DBIO[A])(f: A ⇒ B): DBIO[B] = fa.map(f)
  def coflatMap[A, B](fa: DBIO[A])(f: DBIO[A] ⇒ B): DBIO[B] = DBIO.successful(f(fa))
}

private[cats] class DBIOSemigroup[A: Semigroup](implicit ec: ExecutionContext) extends Semigroup[DBIO[A]] {
  def combine(fx: DBIO[A], fy: DBIO[A]): DBIO[A] =
    (fx zip fy).map { case (x, y) ⇒ x |+| y }
}

private[cats] class DBIOMonoid[A](implicit A: Monoid[A], ec: ExecutionContext) extends DBIOSemigroup[A] with Monoid[DBIO[A]] {
  def empty: DBIO[A] =
    DBIO.successful(A.empty)
}

private[cats] class DBIOGroup[A](implicit A: Group[A], ec: ExecutionContext) extends DBIOMonoid[A] with Group[DBIO[A]] {
  def inverse(fx: DBIO[A]): DBIO[A] =
    fx.map(_.inverse)
  override def remove(fx: DBIO[A], fy: DBIO[A]): DBIO[A] =
    (fx zip fy).map { case (x, y) ⇒ x |-| y }
}