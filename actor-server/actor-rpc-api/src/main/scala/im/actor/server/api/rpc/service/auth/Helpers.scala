package im.actor.server.api.rpc.service.auth

import java.util.regex.Pattern

import im.actor.api.rpc._
import scala.concurrent.forkjoin.ThreadLocalRandom

import scalaz._, syntax.all._
import slick.dbio._

private[auth] trait Helpers extends PublicKeyHelpers {
  def nonEmptyString(s: String): \/[NonEmptyList[String], String] = {
    val trimmed = s.trim
    if (trimmed.isEmpty) "Should be nonempty".wrapNel.left else trimmed.right
  }

  def printableString(s: String): \/[NonEmptyList[String], String] = {
    val p = Pattern.compile("\\p{Print}+", Pattern.UNICODE_CHARACTER_CLASS)
    if (p.matcher(s).matches) s.right else "Should contain printable characters only".wrapNel.left
  }

  def validName(n: String): \/[NonEmptyList[String], String] =
    nonEmptyString(n).flatMap(printableString)

  def validPublicKey(k: Array[Byte]): \/[NonEmptyList[String], Array[Byte]] =
    if (k.isEmpty) "Should be nonempty".wrapNel.left else k.right

  def validationFailed(errorName: String, errors: NonEmptyList[String]): RpcError =
    RpcError(400, errorName, errors.toList.mkString(", "), false, None)

  def withValidName[A, E <: Effect](n: String)(f: String ⇒ DBIOAction[RpcError \/ A, NoStream, E]): DBIOAction[RpcError \/ A, NoStream, E] =
    validName(n).fold(
      x ⇒
        DBIO.successful(Error(validationFailed("NAME_INVALID", x))),
      f
    )

  def withValidPublicKey[A, E <: Effect](k: Array[Byte])(f: Array[Byte] ⇒ DBIOAction[RpcError \/ A, NoStream, E]): DBIOAction[RpcError \/ A, NoStream, E] =
    validPublicKey(k).fold(
      x ⇒
        DBIO.successful(Error(validationFailed("PUBLIC_KEY_INVALID", x))),
      f
    )
}
