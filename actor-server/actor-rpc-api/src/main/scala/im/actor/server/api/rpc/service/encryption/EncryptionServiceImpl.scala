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

object EncryptionServiceErrors {
  val OwnNotPresent = RpcError(400, "OWN_KEYS_NOT_PRESENT", "Own keys are not present", canTryAgain = false, None)
}

final class EncryptionServiceImpl(implicit system: ActorSystem) extends EncryptionService {
  import PeerHelpers._

  override implicit protected val ec: ExecutionContext = system.dispatcher
  private val encExt = EncryptionExtension(system)
  private val db = DbExtension(system).db
  private val updExt = SeqUpdatesExtension(system)

  override def jhandleLoadPublicKeyGroups(
    userPeer:   ApiUserOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeyGroups]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeerF(userPeer) {
        for {
          keyGroups ← encExt.fetchApiKeyGroups(userPeer.userId)
        } yield Ok(ResponsePublicKeyGroups(keyGroups))
      }
    }

  override def jhandleCreateNewKeyGroup(
    identityKey:          ApiEncryptionKey,
    supportedEncryptions: IndexedSeq[String],
    keys:                 IndexedSeq[ApiEncryptionKey],
    signatures:           IndexedSeq[ApiEncryptionKeySignature],
    clientData:           ClientData
  ): Future[HandlerResult[ResponseCreateNewKeyGroup]] =
    authorized(clientData) { client ⇒
      for {
        id ← encExt.createKeyGroup(client.userId, client.authSid, supportedEncryptions, identityKey, keys, signatures)
      } yield Ok(ResponseCreateNewKeyGroup(id))
    }

  override def jhandleDeleteKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      for {
        _ ← encExt.deleteKeyGroup(client.userId, keyGroupId)
      } yield Ok(ResponseVoid)
    }

  override def jhandleLoadPublicKey(
    userPeer:   ApiUserOutPeer,
    keyGroupId: Int,
    keyIds:     IndexedSeq[Long],
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeys]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeerF(userPeer) {
        val keyIdsSet = keyIds.toSet

        for {
          (ks, signs) ← encExt.fetchApiKeys(userPeer.userId, keyGroupId, keyIdsSet)
          (eks, esigns) ← encExt.fetchApiEphermalKeys(userPeer.userId, keyGroupId, keyIdsSet)
        } yield Ok(ResponsePublicKeys(ks ++ eks, signs ++ esigns))
      }
    }

  override def jhandleUploadPreKey(
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

  override def jhandleLoadPrePublicKeys(
    userPeer:   ApiUserOutPeer,
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponsePublicKeys]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeerF(userPeer) {
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

  override def jhandleSendEncryptedPackage(
    randomId:         Long,
    destPeers:        IndexedSeq[ApiUserOutPeer],
    ignoredKeyGroups: IndexedSeq[ApiKeyGroupId],
    encryptedBox:     ApiEncryptedBox,
    clientData:       ClientData
  ): Future[HandlerResult[ResponseSendEncryptedPackage]] =
    authorized(clientData) { implicit client ⇒
      db.run {
        withUserOutPeers(destPeers) {
          DBIO.from {
            encExt.checkBox(encryptedBox) flatMap {
              case Left((missing, obs)) ⇒
                FastFuture.successful(Ok(ResponseSendEncryptedPackage(
                  seq = None,
                  state = None,
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

                owns.headOption match {
                  case Some((_, ownMapping)) ⇒
                    val peersFu =
                      Future.sequence(peers map {
                        case (userId, mapping) ⇒
                          updExt.deliverMappedUpdate(userId, Some(UpdateEmptyUpdate), mapping.toMap)
                      })

                    for {
                      _ ← peersFu
                      seqstate ← updExt.deliverMappedUpdate(client.userId, Some(UpdateEmptyUpdate), ownMapping)
                    } yield Ok(ResponseSendEncryptedPackage(
                      seq = Some(seqstate.seq),
                      state = Some(seqstate.state.toByteArray),
                      date = Some(date),
                      Vector.empty,
                      Vector.empty
                    ))
                  case None ⇒
                    Future.successful(Error(EncryptionServiceErrors.OwnNotPresent))
                }
            }
          }
        }
      }
    }

  override def jhandleDisconnectKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = Future.failed(new RuntimeException("Not implemented"))

  override def jhandleConnectKeyGroup(
    keyGroupId: Int,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = Future.failed(new RuntimeException("Not implemented"))
}