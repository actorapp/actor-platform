package im.actor.api.rpc

import cats.{ Monad, Functor }

sealed trait MaybeAuthorized[+A] {
  private def flatten[B](xs: MaybeAuthorized[MaybeAuthorized[B]]): MaybeAuthorized[B] =
    xs match {
      case Authorized(Authorized(x)) ⇒ Authorized(x)
      case Authorized(NotAuthorized) ⇒ NotAuthorized
      case NotAuthorized             ⇒ NotAuthorized
    }

  def flatMap[B](f: A ⇒ MaybeAuthorized[B]): MaybeAuthorized[B] = flatten(map(f))

  def map[B](f: A ⇒ B): MaybeAuthorized[B] =
    this match {
      case Authorized(a) ⇒ Authorized(f(a))
      case NotAuthorized ⇒ NotAuthorized
    }

  def getOrElse[B >: A](default: ⇒ B): B =
    this match {
      case Authorized(a) ⇒ a
      case NotAuthorized ⇒ default
    }
}

@SerialVersionUID(1L)
final case class Authorized[+A](a: A) extends MaybeAuthorized[A]

@SerialVersionUID(1L)
case object NotAuthorized extends MaybeAuthorized[Nothing]

@SerialVersionUID(1L)
case object MaybeAuthorized extends MaybeAuthorizedInstances

trait MaybeAuthorizedInstances {
  implicit val maybeAuthorizedInstance = new Functor[MaybeAuthorized] with Monad[MaybeAuthorized] {
    override def map[A, B](fa: MaybeAuthorized[A])(f: A ⇒ B): MaybeAuthorized[B] = fa.map(f)
    def pure[A](a: A): MaybeAuthorized[A] = Authorized(a)
    def flatMap[A, B](fa: MaybeAuthorized[A])(f: A ⇒ MaybeAuthorized[B]): MaybeAuthorized[B] = fa.flatMap(f)
    def tailRecM[A, B](a: A)(f: A ⇒ MaybeAuthorized[Either[A, B]]): MaybeAuthorized[B] = defaultTailRecM(a)(f)
  }
}
