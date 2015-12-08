package im.actor.server.persist

import slick.lifted.Tag
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.StickerPack

class StickerPackTable(tag: Tag) extends Table[StickerPack](tag, "sticker_packs") {
  def id = column[Int]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def ownerUserId = column[Int]("owner_user_id")
  def isDefault = column[Boolean]("is_default")

  def * = (id, accessSalt, ownerUserId, isDefault) <> (StickerPack.tupled, StickerPack.unapply)
}

object StickerPackRepo {
  val stickerPacks = TableQuery[StickerPackTable]

  def create(pack: StickerPack) =
    stickerPacks += pack

  def setDefault(packId: Int, isDefault: Boolean): DBIO[Int] =
    stickerPacks.filter(_.id === packId).map(_.isDefault).update(isDefault)

  def findByOwner(userId: Int): DBIO[Seq[StickerPack]] =
    stickerPacks.filter(_.ownerUserId === userId).result

  def exists(userId: Int, packId: Int): DBIO[Boolean] =
    stickerPacks.filter(p ⇒ (p.id === packId) && (p.ownerUserId === userId)).exists.result

  def exists(packId: Int): DBIO[Boolean] =
    stickerPacks.filter(_.id === packId).exists.result

  def find(id: Int) = stickerPacks.filter(_.id === id).result.headOption

  def find(ids: Seq[Int], withDefault: Boolean = true): DBIO[Seq[StickerPack]] =
    (if (withDefault)
      stickerPacks.filter(p ⇒ p.isDefault || (p.id inSet ids.toSet))
    else
      stickerPacks.filter(p ⇒ p.id inSet ids)).result
}
