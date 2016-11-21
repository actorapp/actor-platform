package im.actor.server.api.rpc.service.encryption

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.encryption._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.ApiUserOutPeer
import im.actor.api.rpc.sequence.UpdateEmptyUpdate
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.encryption.EncryptionExtension
import im.actor.server.sequence.SeqUpdatesExtension

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

final class EncryptionServiceImpl(implicit system: ActorSystem) extends EncryptionService {
  import PeerHelpers._

  override implicit protected val ec: ExecutionContext = system.dispatcher
  private val encExt = EncryptionExtension(system)
  private val db = DbExtension(system).db
  private val updExt = SeqUpdatesExtension(system)

  override def doHandleLoadPublicKeyGroups(
    userPeer:   ApiUserOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeyGroups]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeer(userPeer) {
        for {
          keyGroups ← encExt.fetchApiKeyGroups(userPeer.userId)
        } yield Ok(ResponsePublicKeyGroups(keyGroups))
      }
    }

  override def doHandleCreateNewKeyGroup(
    identityKey:          ApiEncryptionKey,
    supportedEncryptions: IndexedSeq[String],
    keys:                 IndexedSeq[ApiEncryptionKey],
    signatures:           IndexedSeq[ApiEncryptionKeySignature],
    clientData:           ClientData
  ): Future[HandlerResult[ResponseCreateNewKeyGroup]] =
    authorized(clientData) { client ⇒
      for {
        id ← encExt.createKeyGroup(client.userId, client.authId, supportedEncryptions, identityKey, keys, signatures)
      } yield Ok(ResponseCreateNewKeyGroup(id))
    }

  override def doHandleDeleteKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← encExt.deleteKeyGroup(client.userId, keyGroupId)
      } yield Ok(ResponseVoid)
    }

  override def doHandleLoadPublicKey(
    userPeer:   ApiUserOutPeer,
    keyGroupId: Int,
    keyIds:     IndexedSeq[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeys]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeer(userPeer) {
        val keyIdsSet = keyIds.toSet

        for {
          (ks, signs) ← encExt.fetchApiKeys(userPeer.userId, keyGroupId, keyIdsSet)
          (eks, esigns) ← encExt.fetchApiEphermalKeys(userPeer.userId, keyGroupId, keyIdsSet)
        } yield Ok(ResponsePublicKeys(ks ++ eks, signs ++ esigns))
      }
    }

  override def doHandleUploadPreKey(
    keyGroupId: Int,
    keys:       IndexedSeq[ApiEncryptionKey],
    signatures: IndexedSeq[ApiEncryptionKeySignature],
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← encExt.createEphermalKeys(client.userId, keyGroupId, keys.toVector, signatures.toVector)
      } yield Ok(ResponseVoid)
    }

  override def doHandleLoadPrePublicKeys(
    userPeer:   ApiUserOutPeer,
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeys]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeer(userPeer) {
        for {
          (keys, signs) ← encExt.fetchApiEphermalKeys(userPeer.userId, keyGroupId)
        } yield {
          val (respKeys, respSigns) =
            if (keys.nonEmpty) {
              val key = keys(ThreadLocalRandom.current().nextInt(keys.length))
              val respSigns = signs filter (_.keyId == key.keyId)
              (Vector(key), respSigns)
            } else (keys, signs)

          Ok(ResponsePublicKeys(respKeys, respSigns))
        }
      }
    }

  override def doHandleSendEncryptedPackage(
    randomId:         Long,
    destPeers:        IndexedSeq[ApiUserOutPeer],
    ignoredKeyGroups: IndexedSeq[ApiKeyGroupId],
    encryptedBox:     ApiEncryptedBox,
    clientData:       ClientData
  ): Future[HandlerResult[ResponseSendEncryptedPackage]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeers(destPeers) {
        encExt.checkBox(encryptedBox, ignoredKeyGroups.groupBy(_.userId).mapValues(_.map(_.keyGroupId).toSet)) flatMap {
          case Left((missing, obs)) ⇒
            FastFuture.successful(Ok(ResponseSendEncryptedPackage(
              date = None,
              obsoleteKeyGroups = obs,
              missedKeyGroups = missing
            )))
          case Right(mappedBoxes) ⇒
            val date = System.currentTimeMillis()
            val mappings = mappedBoxes
              .map {
                case (userId, authIdsBoxes) ⇒
                  (
                    userId,
                    authIdsBoxes.map {
                      case (authId, box) ⇒ (authId, UpdateEncryptedPackage(randomId, date, client.userId, box))
                    }.toMap
                  )
              }

            val (owns, peers) = mappings.partition(_._1 == client.userId)
            val ownOpt = owns.headOption map (_._2)

            val peersFu =
              Future.sequence(peers map {
                case (userId, mapping) ⇒
                  // TODO: does this actually works?
                  updExt.deliverCustomUpdate(userId, 0L, Some(UpdateEmptyUpdate), mapping)
              })

            for {
              _ ← peersFu
              seqState ← ownOpt match {
                case Some(own) ⇒
                  // TODO: does this actually works?
                  updExt.deliverCustomUpdate(client.userId, client.authId, Some(UpdateEmptyUpdate), own)
                case None ⇒ updExt.deliverClientUpdate(client.userId, client.authId, UpdateEmptyUpdate)
              }
            } yield Ok(ResponseSendEncryptedPackage(
              date = Some(date),
              Vector.empty,
              Vector.empty
            ))
        }
      }
    }

  override def doHandleDisconnectKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = Future.failed(new RuntimeException("Not implemented"))

  override def doHandleConnectKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = Future.failed(new RuntimeException("Not implemented"))
}
