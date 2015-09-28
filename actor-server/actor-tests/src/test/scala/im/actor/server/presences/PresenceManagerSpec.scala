package im.actor.server.presences

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.time.{ Seconds, Span }

import im.actor.server.ActorSuite
import im.actor.server.db.DbExtension

import scala.util.Random

final class PresenceManagerSpec extends ActorSuite {
  behavior of "PresenceManager"

  it should "subscribe to presences" in e1
  it should "send presence on subscription" in e2
  it should "deliver presence changes" in e3
  it should "change presence to Offline after timeout" in e4
  it should "correctly calculate multi-device presences" in e5
  it should "not set user offline on explicit offline from only one device" in e6

  import PresenceManager._
  import Presences._

  implicit val ec: ExecutionContext = system.dispatcher

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  implicit val timeout: Timeout = Timeout(5.seconds)

  DbExtension(system).clean()
  DbExtension(system).migrate()

  implicit val region = PresenceManager.startRegion()

  val probe = TestProbe()
  val userId = 1

  def e1() = {
    whenReady(subscribe(userId, probe.ref)) { _ ⇒ }
  }

  def e2() = {
    probe.expectMsg(PresenceState(userId, Offline, None))
  }

  def e3() = {
    presenceSetOnline(userId, 1L, 500)
    val lastSeenAt = probe.expectMsgPF() {
      case PresenceState(1, Online, Some(ls)) ⇒
        ls
    }

    presenceSetOffline(userId, 1L, 100)
    probe.expectMsgPF() {
      case PresenceState(1, Offline, Some(ls)) ⇒
        ls should ===(lastSeenAt)
    }
  }

  def e4() = {
    presenceSetOnline(userId, 1L, 100)
    val lastSeenAt = probe.expectMsgPF() {
      case PresenceState(1, Online, Some(ls)) ⇒
        ls
    }

    Thread.sleep(200)

    probe.expectMsgPF() {
      case PresenceState(1, Offline, Some(ls)) ⇒
        ls should ===(lastSeenAt)
    }
  }

  def e5() = {
    presenceSetOnline(userId, 1L, 200)
    probe.expectMsgPF() {
      case PresenceState(1, Online, Some(ls)) ⇒ ls
    }

    presenceSetOnline(userId, 2L, 400)

    probe.expectNoMsg(300.millis)

    probe.expectMsgPF() {
      case PresenceState(1, Offline, Some(ls)) ⇒ ls
    }

    probe.expectNoMsg()

    presenceSetOnline(userId, 1L, 200)
    presenceSetOnline(userId, 2L, 400)

    probe.expectMsgPF() {
      case PresenceState(1, Online, Some(ls)) ⇒ ls
    }

    probe.expectNoMsg(300.millis)

    probe.expectMsgPF() {
      case PresenceState(1, Offline, Some(ls)) ⇒ ls
    }

    probe.expectNoMsg()
  }

  def e6() = {
    val probe = TestProbe()
    val userId = Random.nextInt()
    whenReady(subscribe(userId, probe.ref)) { _ ⇒ }
    probe.expectMsgPF() {
      case PresenceState(`userId`, Offline, None) ⇒
    }

    presenceSetOnline(userId, 1L, 200)
    presenceSetOnline(userId, 2L, 200)
    presenceSetOffline(userId, 1L, 0)
    probe.expectMsgPF() {
      case PresenceState(`userId`, Online, Some(ls)) ⇒ ls
    }

    probe.expectNoMsg(100.millis)
    probe.expectMsgPF() {
      case PresenceState(`userId`, Offline, Some(_)) ⇒
    }

    presenceSetOnline(userId, 1L, 200)
    probe.expectMsgPF() {
      case PresenceState(`userId`, Online, Some(_)) ⇒
    }
    probe.expectMsgPF() {
      case PresenceState(`userId`, Offline, Some(_)) ⇒
    }

    probe.expectNoMsg()
  }
}
