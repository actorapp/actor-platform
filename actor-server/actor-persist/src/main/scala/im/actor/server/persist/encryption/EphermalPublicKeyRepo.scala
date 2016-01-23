package im.actor.server.persist.encryption

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.encryption.EphermalPublicKey

final class EphermalPublicKeyTable(tag: Tag) extends Table[EphermalPublicKey](tag, "ephermal_public_keys") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def keyGroupId = column[Int]("key_group_id", O.PrimaryKey)
  def keyId = column[Long]("key_id", O.PrimaryKey)
  def body = column[Array[Byte]]("body")

  def * = (userId, keyGroupId, keyId, body) <> ((apply _).tupled, unapply)

  private def apply(userId: Int, keyGroupId: Int, keyId: Long, body: Array[Byte]): EphermalPublicKey = {
    val pk = EphermalPublicKey.parseFrom(body)
    require(pk.key.isDefined)
    require(pk.key.exists(_.id == keyId))
    pk
  }

  private def unapply(pk: EphermalPublicKey): Option[(Int, Int, Long, Array[Byte])] =
    for {
      encKey ← pk.key
    } yield (pk.userId, pk.keyGroupId, encKey.id, pk.toByteArray)
}

object EphermalPublicKeyRepo {
  val ephermalPublicKeys = TableQuery[EphermalPublicKeyTable]

  def byUserId(userId: Rep[Int]) = ephermalPublicKeys filter (_.userId === userId)
  val byUserIdC = Compiled(byUserId _)

  def byUserIdKeyGroup(userId: Rep[Int], keyGroupId: Rep[Int]) =
    byUserId(userId).filter(_.keyGroupId === keyGroupId)
  val byUserIdKeyGroupC = Compiled(byUserIdKeyGroup _)

  def create(pk: EphermalPublicKey) = ephermalPublicKeys += pk

  def create(pks: Seq[EphermalPublicKey]) = ephermalPublicKeys ++= pks

  def fetch(userId: Int) = byUserIdC(userId)

  def fetch(userId: Int, keyGroupId: Int) = byUserIdKeyGroupC(userId → keyGroupId).result

  def fetch(userId: Int, keyGroupId: Int, keyIds: Set[Long]) =
    byUserIdKeyGroupC.applied(userId → keyGroupId).filter(_.keyId inSet keyIds).result
}