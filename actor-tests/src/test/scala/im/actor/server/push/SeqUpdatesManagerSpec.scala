package im.actor.server.push

import scala.concurrent.duration._

import akka.testkit._
import com.typesafe.config._

import im.actor.api.{ rpc ⇒ api }
import im.actor.server.{ KafkaSpec, SqlSpecHelpers }
import im.actor.server.api.ActorSpecHelpers
import im.actor.util.testing._

class SeqUpdatesManagerSpec extends ActorSuite(
  {
    ActorSpecification.createSystem(
      ConfigFactory.parseString("""
        push.seq-updates-manager.receive-timeout = 1 second
                                """)
    )
  }
) with SqlSpecHelpers with ActorSpecHelpers with KafkaSpec {
  behavior of "SeqUpdatesManager"

  it should "increment seq on update push" in e1

  import SeqUpdatesManager._

  implicit val (ds, db) = migrateAndInitDb()

  val region = buildSeqUpdManagerRegion()
  val probe = TestProbe()

  def e1() = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))
    val (userIds, groupIds) = updateRefs(update)

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)))
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SequenceState]
      msg._1 should ===(1000)
    }

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 should ===(1001)
    }

    probe.expectNoMsg(3.seconds)

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 should ===(2000)
    }

    for (a ← 1 to 600)
      probe.send(region.ref, Envelope(authId, PushUpdate(update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)))

    probe.expectNoMsg(4.seconds)

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, userIds, groupIds, None, None, isFat = false)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 should ===(3500)
    }
  }

  override def afterAll: Unit = {
    super.afterAll()
    system.awaitTermination()
    closeDb()
  }

  private def closeDb(): Unit =
    ds.close()
}
