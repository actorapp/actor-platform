package im.actor.server.encryption

import cats.Foldable
import cats.data.Xor, Xor._
import cats.std.all._
import cats.syntax.all._
import com.google.protobuf.ByteString
import im.actor.api.rpc.encryption.{ ApiEncryptionKeySignature, ApiEncryptionKeyGroup, ApiEncryptionKey }
import im.actor.server.model.encryption.{ EphermalPublicKey, EncryptionKeySignature, EncryptionKeyGroup, EncryptionKey }

import scala.language.implicitConversions
import scala.util.control.NoStackTrace

abstract class EncryptionError(message: String) extends RuntimeException(message) with NoStackTrace
class EncryptionValidationError(message: String) extends EncryptionError(message)
case object KeyNotPresent extends EncryptionValidationError("Key is not present")

trait EncryptionApiConverters {
  type ValidatedValue[+T] = Xor[EncryptionValidationError, T]

  def toModel(s: ApiEncryptionKeySignature): ValidatedValue[EncryptionKeySignature] =
    right(EncryptionKeySignature(
      keyId = s.keyId,
      alg = s.signatureAlg,
      signature = ByteString.copyFrom(s.signature)
    ))

  def toModel(k: ApiEncryptionKey): ValidatedValue[EncryptionKey] = right(EncryptionKey(
    id = k.keyId,
    alg = k.keyAlg,
    material = k.keyMaterial.map(ByteString.copyFrom).getOrElse(ByteString.EMPTY),
    hash = k.keyHash.map(ByteString.copyFrom).getOrElse(ByteString.EMPTY)
  ))

  def toModel(kg: ApiEncryptionKeyGroup, userId: Int): ValidatedValue[EncryptionKeyGroup] =
    for {
      identityKey ← toModel(kg.identityKey)
      keys ← kg.keys.toVector.traverseU(toModel)
      signs ← kg.signatures.toVector.traverseU(toModel)
    } yield EncryptionKeyGroup(
      userId = userId,
      id = kg.keyGroupId,
      supportedEncryptions = kg.supportedEncryption,
      identityKey = Some(identityKey),
      keys = keys,
      signatures = signs
    )

  def toApi(k: EncryptionKey): ValidatedValue[ApiEncryptionKey] = right(ApiEncryptionKey(
    keyId = k.id,
    keyAlg = k.alg,
    keyMaterial = Some(k.material.toByteArray),
    keyHash = Some(k.hash.toByteArray)
  ))

  def toApi(ks: EncryptionKeySignature): ValidatedValue[ApiEncryptionKeySignature] = right(ApiEncryptionKeySignature(
    ks.keyId,
    ks.alg,
    ks.signature.toByteArray
  ))

  def toApi(kg: EncryptionKeyGroup): ValidatedValue[ApiEncryptionKeyGroup] =
    for {
      identity ← kg.identityKey map toApi getOrElse error("Identity is absent")
      keys ← kg.keys.toVector.traverseU(toApi)
      signs ← kg.signatures.toVector.traverseU(toApi)
    } yield ApiEncryptionKeyGroup(
      keyGroupId = kg.id,
      supportedEncryption = kg.supportedEncryptions.toVector,
      identityKey = identity,
      keys = keys,
      signatures = signs
    )

  type ApiEphermalPublicKeys = (Vector[ApiEncryptionKey], Vector[ApiEncryptionKeySignature])

  def toApi(eks: Seq[EphermalPublicKey]): ValidatedValue[ApiEphermalPublicKeys] =
    for {
      apiPairs ← eks.toVector.traverseU(toApi)
    } yield apiPairs.foldLeft[ApiEphermalPublicKeys](Vector.empty, Vector.empty) {
      case (acc, (key, signs)) ⇒
        (acc._1 :+ key, acc._2 ++ signs)
    }

  def toApi(ek: EphermalPublicKey): ValidatedValue[(ApiEncryptionKey, Vector[ApiEncryptionKeySignature])] =
    for {
      key ← ek.key map toApi getOrElse Xor.left(KeyNotPresent)
      signs ← ek.signatures.toVector.traverseU(toApi)
    } yield key → signs

  private def error[T](message: String): ValidatedValue[T] = left(new EncryptionValidationError(message))
}
