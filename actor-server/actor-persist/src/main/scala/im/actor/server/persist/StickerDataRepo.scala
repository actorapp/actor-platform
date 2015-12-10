package im.actor.server.persist

import im.actor.server.model.StickerData
import im.actor.server.db.ActorPostgresDriver.api._
import slick.dbio.Effect.Write
import slick.profile.FixedSqlAction

class StickerDataTable(tag: Tag) extends Table[StickerData](tag, "sticker_data") {
  def id = column[Int]("id", O.PrimaryKey)
  def packId = column[Int]("pack_id", O.PrimaryKey)
  def emoji = column[Option[String]]("emoji")
  def image128FileId = column[Long]("image_128_file_id")
  def image128FileHash = column[Long]("image_128_file_hash")
  def image128FileSize = column[Long]("image_128_file_size")
  def image256FileId = column[Option[Long]]("image_256_file_id")
  def image256FileHash = column[Option[Long]]("image_256_file_hash")
  def image256FileSize = column[Option[Long]]("image_256_file_size")
  def image512FileId = column[Option[Long]]("image_512_file_id")
  def image512FileHash = column[Option[Long]]("image_512_file_hash")
  def image512FileSize = column[Option[Long]]("image_512_file_size")

  def * = (
    id,
    packId,
    emoji,
    image128FileId,
    image128FileHash,
    image128FileSize,
    image256FileId,
    image256FileHash,
    image256FileSize,
    image512FileId,
    image512FileHash,
    image512FileSize
  ) <> (StickerData.tupled, StickerData.unapply)
}

object StickerDataRepo {

  val stickerData = TableQuery[StickerDataTable]

  def create(data: StickerData): DBIO[Int] = stickerData += data

  def find(id: Int) = stickerData.filter(_.id === id).result

  def findByPack(packId: Int) = stickerData.filter(_.packId === packId).result

  def delete(packId: Int, stickerId: Int) =
    stickerData.filter(s ⇒ s.packId === packId && s.id === stickerId).delete

}