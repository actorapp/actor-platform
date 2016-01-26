package im.actor.server.persist.encryption

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.encryption.EncryptionKeyGroup

final class EncryptionKeyGroupTable(tag: Tag) extends Table[EncryptionKeyGroup](tag, "encryption_key_groups") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def id = column[Int]("id", O.PrimaryKey)

  def body = column[Array[Byte]]("body")

  def * = (userId, id, body) <> ((apply _).tupled, unapply)

  private def apply(userId: Int, id: Int, body: Array[Byte]) = {
    val kg = EncryptionKeyGroup.parseFrom(body)
    require(kg.userId == userId)
    require(kg.id == id)
    kg
  }

  private def unapply(kg: EncryptionKeyGroup): Option[(Int, Int, Array[Byte])] = Some((kg.userId, kg.id, kg.toByteArray))
}

object EncryptionKeyGroupRepo {
  val keyGroups = TableQuery[EncryptionKeyGroupTable]

  def byPK(userId: Rep[Int], id: Rep[Int]) = keyGroups filter (kg ⇒ kg.userId === userId && kg.id === id)
  val byPKC = Compiled(byPK _)

  def pkBKExists(userId: Rep[Int], id: Rep[Int]) = byPK(userId, id).exists
  val byPKExistsC = Compiled(pkBKExists _)

  def byUserId(userId: Rep[Int]) = keyGroups filter (_.userId === userId)
  val byUserIdC = Compiled(byUserId _)

  def create(keyGroup: EncryptionKeyGroup): DBIO[Int] = keyGroups += keyGroup

  def find(userId: Int, id: Int) = byPKC(userId → id).result.headOption

  def fetch(userId: Int) = byUserIdC(userId).result

  def delete(userId: Int, id: Int) = byPKC(userId → id).delete

  def exists(userId: Int, id: Int) = byPKExistsC(userId → id).result
}