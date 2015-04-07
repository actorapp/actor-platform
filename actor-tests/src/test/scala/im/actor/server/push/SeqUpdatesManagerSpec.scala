package im.actor.server.push

import scala.concurrent.duration._

import akka.testkit._
import com.typesafe.config._
import org.specs2.specification.core.Fragments

import im.actor.api.{ rpc => api }
import im.actor.server.SqlSpecHelpers
import im.actor.util.testing._

class SeqUpdatesManagerSpec extends ActorSpecification(
  ActorSpecification.createSystem(
    ConfigFactory.parseString( """
      push.seq-updates-manager.receive-timeout = 1 second
                               """))
) with SqlSpecHelpers {
  def is = s2"""
  SeqUpdatesManager should
    increment seq on update push $e1
  """

  import SeqUpdatesManager._

  implicit val (ds, db) = migrateAndInitDb()

  val region = startRegion()
  val probe = TestProbe()

  def e1 = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))

    {
      probe.send(region, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray)))
      val msg = probe.receiveOne(5.seconds).asInstanceOf[SequenceState]
      msg._1 must be_==(1001)
    }

    {
      probe.send(region, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 must be_==(1002)
    }

    probe.expectNoMsg(1.5.seconds)

    {
      probe.send(region, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 must be_==(2001)
    }

    for (a <- 1 to 600)
      probe.send(region, Envelope(authId, PushUpdate(update.header, update.toByteArray)))

    probe.expectNoMsg(4.seconds)

    {
      probe.send(region, Envelope(authId, PushUpdateGetSequenceState(update.header, update.toByteArray)))
      val msg = probe.receiveOne(1.second).asInstanceOf[SequenceState]
      msg._1 must be_==(3500)
    }
  }

  override def map(fragments: => Fragments) =
    super.map(fragments) ^ step(closeDb())

  private def closeDb(): Unit =
    ds.close()
}
