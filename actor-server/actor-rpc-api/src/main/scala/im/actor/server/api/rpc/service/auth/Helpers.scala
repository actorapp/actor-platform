package im.actor.server.api.rpc.service.auth

import cats.data.{ NonEmptyList, Xor }
import cats.syntax.all._
import im.actor.api.rpc._
import im.actor.util.misc.StringUtils
import org.apache.commons.validator.routines.EmailValidator

private[auth] trait Helpers extends PublicKeyHelpers {

  private def matchesEmail(s: String): NonEmptyList[String] Xor String =
    if (EmailValidator.getInstance().isValid(s)) s.right else NonEmptyList("Should be valid email address").left

  def validEmail(email: String): NonEmptyList[String] Xor String =
    StringUtils.nonEmptyString(email).flatMap(e ⇒ matchesEmail(e.toLowerCase))

  private implicit val listMonadCombine = new cats.MonadCombine[List] {
    def pure[A](x: A): List[A] = List(x)
    def combineK[A](x: List[A], y: List[A]): List[A] = x ::: y
    def flatMap[A, B](fa: List[A])(f: (A) ⇒ List[B]): List[B] = fa flatMap f
    def empty[A]: List[A] = List.empty[A]
  }

  def validationFailed(errorName: String, errors: NonEmptyList[String]): RpcError =
    RpcError(400, errorName, errors.unwrap.mkString(", "), false, None)
}
