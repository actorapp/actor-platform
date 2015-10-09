package im.actor.server.presences

import akka.actor.PoisonPill
import akka.testkit.TestProbe
import akka.util.Timeout
import im.actor.server.ActorSuite
import im.actor.server.db.DbExtension
import org.scalatest.time.{ Seconds, Span }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class GroupPresenceManagerSpec extends ActorSuite {
  behavior of "GroupPresenceManager"

  it should "subscribe/unsubscribe to group presences" in e1
  it should "not consider presence change on second device online when first is online" in e2
  it should "not consider presence change on second device offline when first is online" in e3

  implicit val ec: ExecutionContext = system.dispatcher

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  implicit val timeout: Timeout = Timeout(5.seconds)

  DbExtension(system).clean()
  DbExtension(system).migrate()

  implicit val presenceExt = PresenceExtension(system)
  implicit val groupPresenceExt = GroupPresenceExtension(system)

  def e1() = {
    val userId = scala.util.Random.nextInt
    val groupId = scala.util.Random.nextInt
    val probe = TestProbe()

    whenReady(groupPresenceExt.subscribe(groupId, probe.ref)) { _ ⇒ }

    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }

    groupPresenceExt.notifyGroupUserAdded(groupId, userId)

    presenceExt.presenceSetOnline(userId, 1L, 1000)

    probe.expectMsgPF() {
      case GroupPresenceState(g, 1) if g == groupId ⇒
    }

    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }

    whenReady(groupPresenceExt.unsubscribe(groupId, probe.ref)) { _ ⇒ }
    probe.expectNoMsg()

    probe.ref ! PoisonPill
  }

  def e2() = {
    val userId = scala.util.Random.nextInt
    val groupId = scala.util.Random.nextInt
    val probe = TestProbe()

    groupPresenceExt.notifyGroupUserAdded(groupId, userId)

    whenReady(groupPresenceExt.subscribe(groupId, probe.ref)) { _ ⇒ }

    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }

    presenceExt.presenceSetOnline(userId, 1L, 300)

    probe.expectMsgPF() {
      case GroupPresenceState(g, 1) if g == groupId ⇒
    }

    presenceExt.presenceSetOnline(userId, 2L, 600)

    probe.expectNoMsg(400.millis)

    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }
  }

  def e3() = {
    val userId = scala.util.Random.nextInt
    val groupId = scala.util.Random.nextInt
    val probe = TestProbe()

    groupPresenceExt.notifyGroupUserAdded(groupId, userId)

    whenReady(groupPresenceExt.subscribe(groupId, probe.ref)) { _ ⇒ }

    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }

    presenceExt.presenceSetOnline(userId, 1L, 300)

    probe.expectMsgPF() {
      case GroupPresenceState(g, 1) if g == groupId ⇒
    }

    presenceExt.presenceSetOnline(userId, 2L, 300)
    presenceExt.presenceSetOffline(userId, 2L, 300)

    // should not consuder user offline as the first device is still online
    probe.expectNoMsg(200.millis)

    // finally consider user offline as first device's online is timed out
    probe.expectMsgPF() {
      case GroupPresenceState(g, 0) if g == groupId ⇒
    }
  }
}

