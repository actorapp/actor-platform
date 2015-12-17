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
      imageLocation(s.image128FileId, s.image128FileHash, 128, s.image128FileSize),
      optImageLocation(s.image512FileId, s.image512FileHash, 512, s.image512FileSize),
      optImageLocation(s.image256FileId, s.image256FileHash, 256, s.image256FileSize)
    )
  }

  implicit def stickerToApi(stickers: Seq[StickerData]): IndexedSeq[ApiStickerDescriptor] =
    stickers.toVector map stickerToApi

  private def imageLocation(fileId: Long, fileHash: Long, side: Int, fileSize: Long): ApiImageLocation =
    ApiImageLocation(ApiFileLocation(fileId, fileHash), side, side, fileSize.toInt)

  private def optImageLocation(fileId: Option[Long], fileHash: Option[Long], side: Int, fileSize: Option[Long]): Option[ApiImageLocation] =
    for {
      id ← fileId
      hash ← fileHash
      size ← fileSize
    } yield imageLocation(id, hash, side, size)

}
