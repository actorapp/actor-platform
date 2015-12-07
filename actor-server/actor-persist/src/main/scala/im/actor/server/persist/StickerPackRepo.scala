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

  //  def findDefaultPacks = stickerPacks.filter(_.isDefault).result

  //  def findOwnPacks(userId: Int) = stickerPacks.filter(p ⇒ p.isDefault || p.ownerUserId === userId).result

  def find(id: Int) = stickerPacks.filter(_.id === id).result.headOption

  def find(ids: Seq[Int], withDefault: Boolean = true): DBIO[Seq[StickerPack]] =
    (if (withDefault)
      stickerPacks.filter(p ⇒ p.isDefault || (p.id inSet ids.toSet))
    else
      stickerPacks.filter(p ⇒ p.id inSet ids)).result
}
