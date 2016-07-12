package im.actor.server.group

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import com.google.protobuf.wrappers.Int32Value
import im.actor.server.KeyValueMappings
import im.actor.server.db.DbExtension
import im.actor.storage.SimpleStorage
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Storage to keep compatibility between
 * old `shardakka.keyvalue.SimpleKeyValue` storage
 * and new `im.actor.storage.SimpleStorage` storage
 */
final class IntegrationTokensWriteCompat(createdAt: Long)(implicit system: ActorSystem) extends IntegrationTokensWriteOps {

  private val groupV2Ts = GroupExtension(system).GroupV2MigrationTs

  private val (upsert, delete): ((String, Int) ⇒ Future[Unit], String ⇒ Future[Unit]) = if (createdAt > groupV2Ts) {
    val newKV = new IntegrationTokensKeyValueStorage
    (newKV.upsertToken, newKV.deleteToken)
  } else {
    val obsoleteKV = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)
    implicit val timeout = Timeout(20.seconds)
    (obsoleteKV.upsert, obsoleteKV.delete)
  }

  def upsertToken(token: String, groupId: Int): Future[Unit] = upsert(token, groupId)

  def deleteToken(token: String): Future[Unit] = delete(token)
}

/**
 * Storage to keep compatibility between
 * old `shardakka.keyvalue.SimpleKeyValue` storage
 * and new `im.actor.storage.SimpleStorage` storage
 */
final class IntegrationTokensReadCompat(implicit system: ActorSystem) extends IntegrationTokensReadOps {
  import system.dispatcher

  private val newKV = new IntegrationTokensKeyValueStorage

  private val obsoleteKV = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)
  implicit val timeout = Timeout(20.seconds)

  /**
   * Try to find groupId in new key-value,
   * if not found - try to find in obsolete key-value
   */
  def getGroupId(token: String): Future[Option[Int]] = {
    newKV.getGroupId(token) flatMap {
      case Some(id) ⇒ FastFuture.successful(Some(id))
      case None     ⇒ obsoleteKV.get(token)
    }
  }
}

/**
 * Stores mapping "Group id" -> "Integration token"
 * name: String
 * timestamp: Long
 */
private object IntegrationTokensStorage extends SimpleStorage("group_integration_token")

final class IntegrationTokensKeyValueStorage(implicit system: ActorSystem) extends IntegrationTokensOps {
  import system.dispatcher

  val conn = DbExtension(system).connector

  def getGroupId(token: String): Future[Option[Int]] =
    conn.run(
      IntegrationTokensStorage.get(token)
    ) map { optBytes ⇒
        optBytes map (b ⇒ Int32Value.parseFrom(b).value)
      }

  def upsertToken(token: String, groupId: Int): Future[Unit] =
    conn.run(
      IntegrationTokensStorage.upsert(token, Int32Value(groupId).toByteArray)
    ) map (_ ⇒ ())

  def deleteToken(token: String): Future[Unit] =
    conn.run(
      IntegrationTokensStorage.delete(token)
    ) map (_ ⇒ ())
}

trait IntegrationTokensOps extends IntegrationTokensWriteOps with IntegrationTokensReadOps

trait IntegrationTokensWriteOps {
  def upsertToken(token: String, groupId: Int): Future[Unit]
  def deleteToken(token: String): Future[Unit]
}

trait IntegrationTokensReadOps {
  def getGroupId(token: String): Future[Option[Int]]
}
