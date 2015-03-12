package im.actor.server.api.rpc.service.encryption

import akka.actor.ActorSystem

import im.actor.api.rpc._, encryption._
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist

import scala.concurrent._

import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

trait EncryptionServiceImpl extends EncryptionService {
  implicit val actorSystem: ActorSystem
  val db: Database

  override def handleGetPublicKeys(keys: Vector[PublicKeyRequest])(implicit clientData: ClientData): Future[HandlerResult[ResponseGetPublicKeys]] = {
    // TODO: #perf fix thos dirty unperformant code
    val keysSet = keys.toSet
    val keysMap = keys.map(k => k.userId * k.keyHash -> k.accessHash).toMap

    val action = for {
      pkeys <- persist.UserPublicKey.findByUserHashes(keysSet.map(k => (k.userId, k.keyHash)))
      saltsMap <- persist.User.findSalts(keysSet.map(_.userId)) map (_.toMap)
    } yield {
      val items = pkeys.filter { k =>
        val hkey = k.userId * k.hash
        val ahash = util.ACL.userAccessHash(clientData.authId, k.userId, saltsMap.get(k.userId).get) // FIXME: make it safe
        keysMap(hkey) == ahash
      }
      val pubKeys = items.map { key =>
        PublicKey(key.userId, key.hash, key.data)
      }
      Ok(ResponseGetPublicKeys(pubKeys.toVector), Vector.empty)
    }

    db.run(action)
  }
}
