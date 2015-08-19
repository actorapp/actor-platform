package im.actor.server.sequence

import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.server.db.DbExtension
import im.actor.server.{ ImplicitSeqUpdatesManagerRegion, ActorSpecification, ActorSuite }
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
) with ImplicitSeqUpdatesManagerRegion {
  behavior of "SeqUpdatesManager"

  it should "increment seq on update push" in e1

  it should "not reply with seq of the ongoing update (concurrency problem)" in e2

  import SeqUpdatesManagerMessages._

  implicit val timeout: Timeout = Timeout(5.seconds)

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  DbExtension(system).clean()
  DbExtension(system).migrate()

  val region = seqUpdExt.region
  val probe = TestProbe()
  val emptyRefs = UpdateRefs(Seq.empty, Seq.empty)

  def e1() = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))
    val updateBytes = ByteString.copyFrom(update.toByteArray)

    {
      probe.send(region.ref, PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SeqState]
      msg.seq should ===(1000)
    }

    {
      probe.send(region.ref, PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(1001)
    }

    probe.expectNoMsg(3.seconds)

    {
      probe.send(region.ref, PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))
      val msg = probe.receiveOne(1.second).asInstanceOf[SeqState]
      msg.seq should ===(2002)
    }

    for (a ← 1 to 600)
      probe.send(region.ref, PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))

    probe.receiveN(600, 5.seconds)
    probe.expectNoMsg(4.seconds)

    {
      probe.send(region.ref, PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SeqState]
      msg.seq should ===(3603)
    }
  }

  def e2() = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))
    val updateBytes = ByteString.copyFrom(update.toByteArray)

    val futures = for (i ← 0 to 500) yield {
      val f = (region.ref ? PushUpdate(authId, None, update.header, updateBytes, emptyRefs, false, None, None))
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
}
