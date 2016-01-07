package im.actor.server.persist

import java.security.MessageDigest

import com.google.common.primitives.Longs
import com.google.protobuf.ByteString
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.MasterKey

import scala.concurrent.ExecutionContext

final class MasterKeyTable(tag: Tag) extends Table[MasterKey](tag, "master_keys") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def body = column[ByteString]("body")

  def * = (authId, body) <> ((MasterKey.apply _).tupled, MasterKey.unapply)
}

object MasterKeyRepo {
  val masterKeys = TableQuery[MasterKeyTable]

  def create(body: Array[Byte])(implicit ec: ExecutionContext) = {
    val md = MessageDigest.getInstance("SHA-256")
    val authId = Longs.fromByteArray(md.digest(body).take(java.lang.Long.BYTES))
    val masterKey = MasterKey(authId, ByteString.copyFrom(body))
    (masterKeys += masterKey) map (_ ⇒ masterKey)
  }
}