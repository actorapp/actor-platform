package im.actor.server.persist

import im.actor.server.model.AvatarData

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import slick.driver.PostgresDriver.api._
import shapeless._, syntax.std.tuple._

final class AvatarDataTable(tag: Tag) extends Table[AvatarData](tag, "avatar_datas") {
  import AvatarOfColumnType._

  def entityId = column[Long]("entity_id", O.PrimaryKey)
  def entityType = column[AvatarData.TypeVal]("entity_type", O.PrimaryKey)
  def smallAvatarFileId = column[Option[Long]]("small_avatar_file_id")
  def smallAvatarFileHash = column[Option[Long]]("small_avatar_file_hash")
  def smallAvatarFileSize = column[Option[Long]]("small_avatar_file_size")
  def largeAvatarFileId = column[Option[Long]]("large_avatar_file_id")
  def largeAvatarFileHash = column[Option[Long]]("large_avatar_file_hash")
  def largeAvatarFileSize = column[Option[Long]]("large_avatar_file_size")
  def fullAvatarFileId = column[Option[Long]]("full_avatar_file_id")
  def fullAvatarFileHash = column[Option[Long]]("full_avatar_file_hash")
  def fullAvatarFileSize = column[Option[Long]]("full_avatar_file_size")
  def fullAvatarWidth = column[Option[Int]]("full_avatar_width")
  def fullAvatarHeight = column[Option[Int]]("full_avatar_height")

  val adReps = (
    smallAvatarFileId, smallAvatarFileHash, smallAvatarFileSize,
    largeAvatarFileId, largeAvatarFileHash, largeAvatarFileSize,
    fullAvatarFileId, fullAvatarFileHash, fullAvatarFileSize,
    fullAvatarWidth, fullAvatarHeight
  )

  def * = (entityType, entityId) ++ adReps <> (AvatarData.apply _ tupled, AvatarData.unapply)
}

object AvatarDataRepo {
  import AvatarOfColumnType._

  val adatas = TableQuery[AvatarDataTable]

  def create(data: AvatarData) =
    adatas += data

  def createOrUpdate(data: AvatarData) =
    adatas.insertOrUpdate(data)

  def byType(typ: AvatarData.TypeVal) =
    adatas.filter(d ⇒ d.entityType === typ)

  def byTypeAndId(typ: AvatarData.TypeVal, id: Long) =
    byType(typ).filter(d ⇒ d.entityId === id)

  def find(typ: AvatarData.TypeVal, id: Long) =
    byTypeAndId(typ, id).result

  def findByUserId(userId: Int) =
    byTypeAndId(AvatarData.OfUser, userId.toLong).result

  def findByGroupId(groupId: Int) =
    byTypeAndId(AvatarData.OfGroup, groupId.toLong).result.headOption

  def findByUserIds(userIds: Set[Int]) =
    byType(AvatarData.OfUser).filter(d ⇒ d.entityId inSet userIds.map(_.toLong)).result

  def findByGroupIds(groupIds: Set[Int]) =
    byType(AvatarData.OfGroup).filter(d ⇒ d.entityId inSet groupIds.map(_.toLong)).result
}
