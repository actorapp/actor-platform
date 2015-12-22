package im.actor.server.model

case class StickerData(
  id:               Int,
  packId:           Int,
  emoji:            Option[String],
  image128FileId:   Long,
  image128FileHash: Long,
  image128FileSize: Long,
  image128Width:    Int,
  image128Height:   Int,
  image256FileId:   Option[Long],
  image256FileHash: Option[Long],
  image256FileSize: Option[Long],
  image256Width:    Option[Int],
  image256Height:   Option[Int],
  image512FileId:   Option[Long],
  image512FileHash: Option[Long],
  image512FileSize: Option[Long],
  image512Width:    Option[Int],
  image512Height:   Option[Int]
)

case class StickerPack(id: Int, accessSalt: String, ownerUserId: Int, isDefault: Boolean)

case class OwnStickerPack(userId: Int, packId: Int)