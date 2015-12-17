package im.actor.server.persist

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.OwnStickerPack
import slick.dbio.Effect.Read
import slick.profile.FixedSqlStreamingAction

class OwnStickerPackTable(tag: Tag) extends Table[OwnStickerPack](tag, "own_sticker_packs") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def packId = column[Int]("pack_id", O.PrimaryKey)

  def * = (userId, packId) <> (OwnStickerPack.tupled, OwnStickerPack.unapply)
}

object OwnStickerPackRepo {

  val ownStickerPacks = TableQuery[OwnStickerPackTable]

  def create(userId: Int, packId: Int) =
    ownStickerPacks += OwnStickerPack(userId, packId)

  def delete(userId: Int, packId: Int) =
    ownStickerPacks.filter(p ⇒ p.userId === userId && p.packId === packId).delete

  def findPackIds(userId: Int): DBIO[Seq[Int]] = ownStickerPacks.filter(_.userId === userId).map(_.packId).result

  def findUserIds(packId: Int): DBIO[Seq[Int]] = ownStickerPacks.filter(_.packId === packId).map(_.userId).result

  def exists(userId: Int, packId: Int): DBIO[Boolean] =
    ownStickerPacks.filter(p ⇒ p.userId === userId && p.packId === packId).exists.result

}
