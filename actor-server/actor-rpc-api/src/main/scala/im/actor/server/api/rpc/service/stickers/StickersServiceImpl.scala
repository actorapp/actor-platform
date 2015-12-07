package im.actor.server.api.rpc.service.stickers

import akka.actor.ActorSystem
import im.actor.api.rpc.stickers._
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.server.acl.ACLUtils.stickerPackAccessHash
import im.actor.server.db.DbExtension
import im.actor.server.persist
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object StickerPackErrors {
  val PackNotFound = RpcError(404, "PACK_NOT_FOUND", "Sticker pack not found", false, None)
  val AlreadyAdded = RpcError(400, "PACK_ALREADY_ADDED", "Sticker pack already added", false, None)
  val AlreadyRemoved = RpcError(400, "PACK_ALREADY_REMOVED", "Sticker pack already removed", false, None)
}

class StickersServiceImpl(implicit actorSystem: ActorSystem) extends StickersService with ImplicitConversions {

  import FutureResultRpcCats._
  import StickerPackErrors._

  override implicit protected val ec: ExecutionContext = actorSystem.dispatcher

  private val db = DbExtension(actorSystem).db
  private val seqUpdExt = SeqUpdatesExtension(actorSystem)

  override def jhandleLoadStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseLoadStickerCollection]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(persist.StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        stickers ← fromFuture(db.run(persist.StickerDataRepo.findByPack(pack.id)))
      } yield ResponseLoadStickerCollection(ApiStickerCollection(pack.id, accessHash, stickers))).value map (_.toScalaz)
    }

  override def jhandleRemoveStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseStickersReponse]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(persist.StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        _ ← fromFutureBoolean(AlreadyRemoved)(db.run(persist.OwnStickerPackRepo.exists(client.userId, pack.id) map !=))
        _ ← fromFuture(db.run(persist.OwnStickerPackRepo.delete(client.userId, pack.id)))
        seqState ← fromFuture(seqUpdExt.getSeqState(client.userId))
        SeqState(seq, state) = seqState
        stickers ← fromFuture(db.run(getStickerPacks(client.userId)))
      } yield ResponseStickersReponse(stickers, seq, state.toByteArray)).value map (_.toScalaz)
    }

  override def jhandleAddStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseStickersReponse]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(persist.StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        _ ← fromFutureBoolean(AlreadyAdded)(db.run(persist.OwnStickerPackRepo.exists(client.userId, pack.id)))
        _ ← fromFuture(db.run(persist.OwnStickerPackRepo.create(client.userId, pack.id)))
        seqState ← fromFuture(seqUpdExt.getSeqState(client.userId))
        SeqState(seq, state) = seqState
        stickers ← fromFuture(db.run(getStickerPacks(client.userId)))
      } yield ResponseStickersReponse(stickers, seq, state.toByteArray)).value map (_.toScalaz)
    }

  override def jhandleLoadOwnStickers(clientData: ClientData): Future[HandlerResult[ResponseLoadOwnStickers]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      getStickerPacks(client.userId) map (stickers ⇒ Ok(ResponseLoadOwnStickers(stickers)))
    }
    db.run(toDBIOAction(action))
  }

  private def getStickerPacks(userId: Int): DBIO[Vector[ApiStickerCollection]] =
    for {
      packIds ← persist.OwnStickerPackRepo.findPackIds(userId)
      packs ← persist.StickerPackRepo.find(packIds)
      stickerCollections ← DBIO.sequence(packs.toVector map { pack ⇒
        for (stickers ← persist.StickerDataRepo.findByPack(pack.id)) yield ApiStickerCollection(pack.id, stickerPackAccessHash(pack), stickers)
      })
    } yield stickerCollections

}
