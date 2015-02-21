package im.actor.server.persist

import im.actor.server.db.Db
import im.actor.server.models
import slick.driver.PostgresDriver.simple._
import Database.dynamicSession

class AvatarDataTable(tag: Tag) extends Table[models.AvatarData](tag, "avatar_datas") {
  def entityId = column[Long]("phone_number", O.PrimaryKey)
  def entityType = column[String]("entity_type", O.PrimaryKey)
  def smallAvatarFileId = column[Option[Long]]("small_avatar_file_id")
  def smallAvatarFileHash = column[Option[Long]]("small_avatar_file_hash")
  def smallAvatarFileSize = column[Option[Int]]("small_avatar_file_size")
  def largeAvatarFileId = column[Option[Long]]("large_avatar_file_id")
  def largeAvatarFileHash = column[Option[Long]]("large_avatar_file_hash")
  def largeAvatarFileSize = column[Option[Int]]("large_avatar_file_size")
  def fullAvatarFileId = column[Option[Long]]("full_avatar_file_id")
  def fullAvatarFileHash = column[Option[Long]]("full_avatar_file_hash")
  def fullAvatarFileSize = column[Option[Int]]("full_avatar_file_size")
  def fullAvatarWidth = column[Option[Int]]("full_avatar_width")
  def fullAvatarHeight = column[Option[Int]]("full_avatar_height")

  def * = (smallAvatarFileId, smallAvatarFileHash, smallAvatarFileSize, largeAvatarFileId, largeAvatarFileHash,
    largeAvatarFileSize, fullAvatarFileId, fullAvatarFileHash, fullAvatarFileSize, fullAvatarWidth, fullAvatarHeight) <>
    (models.AvatarData.tupled, models.AvatarData.unapply)
}

object AvatarData {
  val table = TableQuery[AvatarDataTable]
}
