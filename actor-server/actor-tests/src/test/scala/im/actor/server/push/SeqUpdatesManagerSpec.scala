package im.actor.server.push

import akka.util.Timeout
import im.actor.server.sequence.SeqState
import im.actor.server.{ ImplicitSeqUpdatesManagerRegion, ActorSpecification, ActorSuite, SqlSpecHelpers }
import org.scalatest.time.{ Span, Seconds }

import scala.concurrent.duration._

import akka.pattern.ask
import akka.testkit._
import com.typesafe.config._

import im.actor.api.{ rpc ⇒ api }

class SeqUpdatesManagerSpec extends ActorSuite(
  {
    ActorSpecification.createSystem(
      ConfigFactory.parseString("""
        push.seq-updates-manager.receive-timeout = 1 second
                                """)
    )
  }
) with SqlSpecHelpers with ImplicitSeqUpdatesManagerRegion {
  behavior of "SeqUpdatesManager"

  it should "increment seq on update push" in e1

  it should "not reply with seq of the ongoing update (concurrency problem)" in e2

  import SeqUpdatesManagerMessages._

  implicit val (ds, db) = migrateAndInitDb()
  implicit val timeout: Timeout = Timeout(5.seconds)

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  val region = seqUpdExt.region
  val probe = TestProbe()

  def e1() = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, None, None, None)))
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SeqState]
      msg.seq should ===(1000)
    }

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, None, None, None)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(1001)
    }

    probe.expectNoMsg(3.seconds)

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, None, None, None)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(2002)
    }

    for (a ← 1 to 600)
      probe.send(region.ref, Envelope(authId, PushUpdate(update.header, update.toByteArray, None, None, None)))

    probe.expectNoMsg(4.seconds)

    {
      probe.send(region.ref, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, None, None, None)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(3603)
    }
  }

  def e2() = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))

    val futures = for (i ← 0 to 100) yield {
      val f = (region.ref ? Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray, None, None, None)))
        .mapTo[SeqState]

      (f, 1000 + i)
    }

    futures foreach {
      case (f, expectedSeq) ⇒
        whenReady(f) { seqstate ⇒
          seqstate.seq shouldEqual expectedSeq
        }
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
