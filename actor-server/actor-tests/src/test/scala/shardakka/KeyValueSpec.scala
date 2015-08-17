package shardakka

import im.actor.server.api.rpc.service.ServiceSpecHelpers
import im.actor.server.db.DbExtension
import im.actor.server.{ ActorSerializerPrepare, ActorSpecification, ActorSuite, ServiceSpecMatchers }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Matchers }

import scala.concurrent.ExecutionContext

final class KeyValueSpec extends ActorSuite(ActorSpecification.createSystem())
  with FlatSpecLike
  with ScalaFutures
  with Matchers
  with ServiceSpecMatchers
  with ServiceSpecHelpers
  with ActorSerializerPrepare {

  it should "set and get values" in setAndGet
  it should "restore state" in restoreState

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(5, Seconds))

  DbExtension(system).clean()
  DbExtension(system).migrate()

  private implicit val ec: ExecutionContext = system.dispatcher

  val ext = ShardakkaExtension(system)

  def setAndGet() = {
    val keyValue = ext.startKeyValueString("setAndGet")

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe empty
    }

    whenReady(keyValue.upsert("key1", "value"))(identity)

    whenReady(keyValue.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }

  def restoreState() = {
    val kvName = "restoreState"

    val keyValue = ext.startKeyValueString(kvName)

    whenReady(keyValue.upsert("key1", "value"))(identity)

    keyValue.shutdown()
    Thread.sleep(200)

    val keyValueNew = ext.startKeyValueString(kvName)

    whenReady(keyValueNew.get("key1")) { resp ⇒
      resp shouldBe Some("value")
    }
  }
}
