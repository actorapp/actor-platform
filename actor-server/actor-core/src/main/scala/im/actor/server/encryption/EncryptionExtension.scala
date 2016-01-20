package im.actor.server.encryption

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import cats.std.all._
import cats.syntax.all._
import cats.data.{ Xor, XorT }
import im.actor.api.rpc.encryption._
import im.actor.cats.dbio._
import im.actor.server.db.DbExtension
import im.actor.server.model.encryption.{ EphermalPublicKey, EncryptionKeyGroup, EncryptionKeySignature, EncryptionKey }
import im.actor.server.persist.encryption.{ EphermalPublicKeyRepo, EncryptionKeyGroupRepo }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.social.SocialExtension
import im.actor.util.misc.IdUtils
import im.actor.server.db.ActorPostgresDriver.api._

import scala.concurrent.Future

final class EncryptionExtension(system: ActorSystem) extends Extension {
  import system.dispatcher
  private val db = DbExtension(system).db
  private val seqUpdExt = SeqUpdatesExtension(system)
  private val socialExt = SocialExtension(system)

  def fetchKeyGroups(userId: Int): Future[Seq[EncryptionKeyGroup]] =
    db.run(EncryptionKeyGroupRepo.fetch(userId))

  def fetchApiKeyGroups(userId: Int): Future[Vector[ApiEncryptionKeyGroup]] = {
    (for {
      kgs ← XorT.right[Future, Exception, Seq[EncryptionKeyGroup]](fetchKeyGroups(userId))
      apiKgs ← XorT.fromXor[Future](kgs.toVector.traverseU(toApi): Xor[Exception, Vector[ApiEncryptionKeyGroup]])
    } yield apiKgs).value map (_.valueOr(throw _))
  }

  type KeysWithSignatures = (Seq[EncryptionKey], Seq[EncryptionKeySignature])

  def fetchKeys(
    userId:     Int,
    keyGroupId: Int,
    keyIds:     Set[Long]
  ): Future[KeysWithSignatures] = {
    for {
      kgOpt ← db.run(EncryptionKeyGroupRepo.find(userId, keyGroupId))
    } yield {
      kgOpt.map { kg ⇒
        (kg.keys.filter(k ⇒ keyIds.contains(k.id)), kg.signatures.filter(ks ⇒ keyIds.contains(ks.keyId)))
      }.getOrElse((Seq.empty, Seq.empty))
    }
  }

  def fetchApiKeys(
    userId:     Int,
    keyGroupId: Int,
    keyIds:     Set[Long]
  ): Future[(Vector[ApiEncryptionKey], Vector[ApiEncryptionKeySignature])] = {
    val futureT =
      for {
        kss ← XorT.right[Future, Exception, KeysWithSignatures](fetchKeys(userId, keyGroupId, keyIds))
        (keys, signs) = kss
        apiKeys ← XorT.fromXor[Future](keys.toVector.traverseU(toApi): Xor[Exception, Vector[ApiEncryptionKey]])
        apiSigns ← XorT.fromXor[Future](signs.toVector.traverseU(toApi): Xor[Exception, Vector[ApiEncryptionKeySignature]])
      } yield (apiKeys, apiSigns)

    futureT.value map (_.valueOr(throw _))
  }

  def createKeyGroup(
    userId:      Int,
    identityKey: EncryptionKey,
    keys:        Seq[EncryptionKey],
    signatures:  Seq[EncryptionKeySignature]
  ): Future[Int] = {
    val id = IdUtils.nextIntId()
    val keyGroup = EncryptionKeyGroup(
      userId = userId,
      id = id,
      identityKey = Some(identityKey),
      keys = keys,
      signatures = signatures
    )

    val actionT = for {
      apiKeyGroup ← XorT.fromXor[DBIO](toApi(keyGroup))
      relatedUserIds ← XorT.right[DBIO, Exception, Set[Int]](DBIO.from(socialExt.getRelations(userId)))
      _ ← XorT.right[DBIO, Exception, Int](EncryptionKeyGroupRepo.create(keyGroup))
      _ ← XorT.right[DBIO, Exception, Any](DBIO.from(seqUpdExt.broadcastSingleUpdate(
        userIds = relatedUserIds,
        update = UpdatePublicKeyGroupAdded(
          userId,
          keyGroup = apiKeyGroup
        )
      )))
    } yield id

    db.run(actionT.value.transactionally) map (_.valueOr(throw _))
  }

  def createKeyGroup(
    userId:         Int,
    apiIdentityKey: ApiEncryptionKey,
    apiKeys:        Seq[ApiEncryptionKey],
    apiSignatures:  Seq[ApiEncryptionKeySignature]
  ): Future[Int] = {
    val futureT = for {
      identityKey ← XorT.fromXor[Future](toModel(apiIdentityKey))
      keys ← XorT.fromXor[Future](apiKeys.toVector.traverseU(toModel))
      signs ← XorT(FastFuture.successful(apiSignatures.toVector.traverseU(toModel)))
      id ← XorT.right[Future, Exception, Int](createKeyGroup(userId, identityKey, keys, signs))
    } yield id

    futureT.value map (_.valueOr(throw _))
  }

  def deleteKeyGroup(userId: Int, id: Int) = {
    val update = UpdatePublicKeyGroupRemoved(userId, id)

    val action =
      for {
        _ ← EncryptionKeyGroupRepo.delete(userId, id)
        relatedUserIds ← DBIO.from(socialExt.getRelations(userId))
        _ ← DBIO.from(seqUpdExt.broadcastSingleUpdate(relatedUserIds, update))
      } yield ()

    db.run(action.transactionally)
  }

  def createEphermalKeys(
    userId:        Int,
    keyGroupId:    Int,
    apiKeys:       Vector[ApiEncryptionKey],
    apiSignatures: Vector[ApiEncryptionKeySignature]
  ) = {
    val apiSignsMap = apiSignatures.groupBy(_.keyId)

    val keysXor =
      (apiKeys map { apiKey ⇒
        for {
          key ← toModel(apiKey)
          signs ← apiSignsMap.getOrElse(key.id, Vector.empty).traverseU(toModel)
        } yield EphermalPublicKey(userId, keyGroupId, Some(key), signs)
      }).sequenceU

    val actionT =
      for {
        _ ← XorT[DBIO, Exception, Unit](for {
          exists ← EncryptionKeyGroupRepo.exists(userId, keyGroupId)
        } yield if (exists) Xor.Right(()) else Xor.Left(new RuntimeException("KeyGroup does not exists")))
        keys ← XorT.fromXor[DBIO](keysXor)
        _ ← XorT.right[DBIO, Exception, Any](EphermalPublicKeyRepo.create(keys))
      } yield ()

    db.run(actionT.value.transactionally) map (_.valueOr(throw _))
  }

  def fetchEphermalKeys(
    userId:     Int,
    keyGroupId: Int
  ): Future[Seq[EphermalPublicKey]] = {
    db.run(for {
      ekeys ← EphermalPublicKeyRepo.fetch(userId, keyGroupId)
    } yield ekeys)
  }

  def fetchApiEphermalKeys(
    userId:     Int,
    keyGroupId: Int
  ): Future[(Vector[ApiEncryptionKey], Vector[ApiEncryptionKeySignature])] = {
    val actionT =
      for {
        ekeys ← XorT.right[Future, Exception, Seq[EphermalPublicKey]](fetchEphermalKeys(userId, keyGroupId))
        apiEKeys ← XorT.fromXor[Future](toApi(ekeys.toVector): Xor[Exception, ApiEphermalPublicKeys])
      } yield apiEKeys

    actionT.value map (_.valueOr(throw _))
  }
}

object EncryptionExtension extends ExtensionId[EncryptionExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): EncryptionExtension = new EncryptionExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = EncryptionExtension
}
