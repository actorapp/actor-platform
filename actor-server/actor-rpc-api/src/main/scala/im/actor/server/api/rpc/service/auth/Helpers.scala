package im.actor.server.api.rpc.service.auth

import scalaz._
import scalaz.syntax.all._

import slick.dbio._

import im.actor.api.rpc._
import im.actor.server.util.StringUtils

private[auth] trait Helpers extends PublicKeyHelpers {
  def validPublicKey(k: Array[Byte]): \/[NonEmptyList[String], Array[Byte]] =
    if (k.isEmpty) "Should be nonempty".wrapNel.left else k.right

  def validationFailed(errorName: String, errors: NonEmptyList[String]): RpcError =
    RpcError(400, errorName, errors.toList.mkString(", "), false, None)

  def withValidName[A, E <: Effect](n: String)(f: String ⇒ DBIOAction[RpcError \/ A, NoStream, E]): DBIOAction[RpcError \/ A, NoStream, E] =
    StringUtils.validName(n).fold(
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
