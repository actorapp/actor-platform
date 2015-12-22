package im.actor.server.stickers

import im.actor.api.rpc.files.{ ApiFileLocation, ApiImageLocation }
import im.actor.api.rpc.stickers.ApiStickerDescriptor
import im.actor.server.model.StickerData

import scala.language.implicitConversions

trait StickersImplicitConversions {

  implicit def stickerToApi(s: StickerData): ApiStickerDescriptor = {
    ApiStickerDescriptor(
      s.id,
      s.emoji,
      imageLocation(s.image128FileId, s.image128FileHash, s.image128Width, s.image128Height, s.image128FileSize),
      optImageLocation(s.image512FileId, s.image512FileHash, s.image512Width, s.image512Height, s.image512FileSize),
      optImageLocation(s.image256FileId, s.image256FileHash, s.image256Width, s.image256Height, s.image256FileSize)
    )
  }

  implicit def stickerToApi(stickers: Seq[StickerData]): IndexedSeq[ApiStickerDescriptor] =
    stickers.toVector map stickerToApi

  private def imageLocation(fileId: Long, fileHash: Long, w: Int, h: Int, fileSize: Long): ApiImageLocation =
    ApiImageLocation(ApiFileLocation(fileId, fileHash), w, h, fileSize.toInt)

  private def optImageLocation(fileId: Option[Long], fileHash: Option[Long], width: Option[Int], height: Option[Int], fileSize: Option[Long]): Option[ApiImageLocation] =
    for {
      id ← fileId
      hash ← fileHash
      size ← fileSize
      w ← width
      h ← height
    } yield imageLocation(id, hash, w, h, size)

}
