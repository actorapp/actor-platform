package im.actor.server.api.rpc.service.stickers

import akka.actor.ActorSystem
import im.actor.api.rpc.stickers._
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.server.acl.ACLUtils.stickerPackAccessHash
import im.actor.server.db.DbExtension
import im.actor.server.persist.{ OwnStickerPackRepo, StickerDataRepo, StickerPackRepo }
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.server.stickers.{ StickersImplicitConversions, StickersExtension }

import scala.concurrent.{ ExecutionContext, Future }

object StickerPackErrors {
  val PackNotFound = RpcError(404, "PACK_NOT_FOUND", "Sticker pack not found", false, None)
  val AlreadyAdded = RpcError(400, "PACK_ALREADY_ADDED", "Sticker pack already added", false, None)
  val AlreadyRemoved = RpcError(400, "PACK_ALREADY_REMOVED", "Sticker pack already removed", false, None)
  val CantAddDefaultPack = RpcError(400, "CANT_ADD_DEFAULT_PACK", "You can't add default pack", false, None)
  val CantRemoveDefaultPack = RpcError(400, "CANT_REMOVE_DEFAULT_PACK", "You can't remove default pack", false, None)
}

class StickersServiceImpl(implicit actorSystem: ActorSystem) extends StickersService with StickersImplicitConversions {

  import FutureResultRpc._
  import StickerPackErrors._

  override implicit protected val ec: ExecutionContext = actorSystem.dispatcher

  private val db = DbExtension(actorSystem).db
  private val seqUpdExt = SeqUpdatesExtension(actorSystem)
  private val stickersExt = StickersExtension(actorSystem)

  override def doHandleLoadStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseLoadStickerCollection]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonRpcErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        stickers ← fromFuture(db.run(StickerDataRepo.findByPack(pack.id)))
      } yield ResponseLoadStickerCollection(ApiStickerCollection(pack.id, accessHash, stickers))).value
    }

  override def doHandleRemoveStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseStickersReponse]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonRpcErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        _ ← fromBoolean(CantRemoveDefaultPack)(!pack.isDefault)
        _ ← fromFutureBoolean(AlreadyRemoved)(db.run(OwnStickerPackRepo.exists(client.userId, pack.id) map !=))
        _ ← fromFuture(db.run(OwnStickerPackRepo.delete(client.userId, pack.id)))
        stickers ← fromFuture(db.run(stickersExt.getOwnApiStickerPacks(client.userId)))
        seqState ← fromFuture(seqUpdExt.deliverClientUpdate(client.userId, client.authId, UpdateOwnStickersChanged(stickers)))
      } yield ResponseStickersReponse(stickers, seqState.seq, seqState.state.toByteArray)).value
    }

  override def doHandleAddStickerCollection(id: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseStickersReponse]] =
    authorized(clientData) { client ⇒
      (for {
        pack ← fromFutureOption(PackNotFound)(db.run(StickerPackRepo.find(id)))
        _ ← fromBoolean(CommonRpcErrors.InvalidAccessHash)(stickerPackAccessHash(pack) == accessHash)
        _ ← fromBoolean(CantAddDefaultPack)(!pack.isDefault)
        _ ← fromFutureBoolean(AlreadyAdded)(db.run(OwnStickerPackRepo.exists(client.userId, pack.id)))
        _ ← fromFuture(db.run(OwnStickerPackRepo.create(client.userId, pack.id)))
        stickers ← fromFuture(db.run(stickersExt.getOwnApiStickerPacks(client.userId)))
        seqState ← fromFuture(seqUpdExt.deliverClientUpdate(client.userId, client.authId, UpdateOwnStickersChanged(stickers)))
      } yield ResponseStickersReponse(stickers, seqState.seq, seqState.state.toByteArray)).value
    }

  override def doHandleLoadOwnStickers(clientData: ClientData): Future[HandlerResult[ResponseLoadOwnStickers]] = {
    authorized(clientData) { implicit client ⇒
      val action = stickersExt.getOwnApiStickerPacks(client.userId) map (stickers ⇒ Ok(ResponseLoadOwnStickers(stickers)))
      db.run(action)
    }
  }

}
