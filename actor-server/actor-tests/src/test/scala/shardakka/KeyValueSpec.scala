package shardakka

import im.actor.server.db.DbExtension
import im.actor.server._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }

import scala.concurrent.{ Future, ExecutionContext }

final class KeyValueSpec extends ActorSuite(ActorSpecification.createSystem())
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ServiceSpecMatchers
  with ServiceSpecHelpers
  with ActorSerializerPrepare {

  it should "set and get values" in setAndGet
  it should "get keys lsit" in keysList
  it should "restore state" in restoreState
  it should "upsert and delete" in upsertAndDelete

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(5, Seconds))

  DbExtension(system).clean()
  DbExtension(system).migrate()

  private implicit val ec: ExecutionContext = system.dispatcher

  val ext = ShardakkaExtension(system)

  def setAndGet() = {
    val keyValue = ext.simpleKeyValue("setAndGet")

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe empty
    }

    whenReady(keyValue.upsert("key1", "value"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }

  def keysList() = {
    val keyValue = ext.simpleKeyValue("keysList")

    whenReady(Future.sequence(Seq(
      keyValue.upsert("key1", "value"),
      keyValue.upsert("key2", "value"),
      keyValue.upsert("key3", "value")
    )))(identity)

    whenReady(keyValue.getKeys()) { keys ⇒
      keys.toSet shouldBe Set("key1", "key2", "key3")
    }
  }

  def restoreState() = {
    val kvName = "restoreState"

    val keyValue = ext.simpleKeyValue(kvName)

    whenReady(keyValue.upsert("key1", "value"))(identity)

    ext.shutdownKeyValue(kvName)
    Thread.sleep(200)

    val keyValueNew = ext.simpleKeyValue(kvName)

    whenReady(keyValueNew.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }

  def upsertAndDelete() = {
    val keyValue = ext.simpleKeyValue("upsertAndDelete")

    whenReady(keyValue.upsert("key1", "value"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }

    whenReady(keyValue.delete("key1"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe empty
    }
  }
}
