package im.actor.server.push

import akka.testkit._
import com.typesafe.config._
import im.actor.api.{ rpc => api }
import im.actor.util.testing._
import scala.concurrent.duration._

class SeqUpdatesManagerSpec extends ActorSpecification(
  ActorSpecification.createSystem(
    ConfigFactory.parseString("""
      push.seq-updates-manager.receive-timeout = 1 second
    """))
) {
  def is = s2"""
  SeqUpdatesManager should
    increment seq on update push $e1
  """

  import SeqUpdatesManager._

  val region = startRegion()
  val probe = TestProbe()

  def e1 = {
    val authId = util.Random.nextLong()
    val update = api.contacts.UpdateContactsAdded(Vector(1, 2, 3))

    probe.send(region, Envelope(authId, PushUpdateGetSeq(update)))
    probe.expectMsg(Seq(1001))

    probe.send(region, Envelope(authId, PushUpdateGetSeq(update)))
    probe.expectMsg(Seq(1002))

    probe.expectNoMsg(1.5.seconds)

    probe.send(region, Envelope(authId, PushUpdateGetSeq(update)))
    probe.expectMsg(Seq(2001))

    for (a <- 1 to 600)
      probe.send(region, Envelope(authId, PushUpdate(update)))

    probe.expectNoMsg(1.5.seconds)

    probe.send(region, Envelope(authId, PushUpdateGetSeq(update)))
    probe.expectMsg(Seq(3501))
  }
}
