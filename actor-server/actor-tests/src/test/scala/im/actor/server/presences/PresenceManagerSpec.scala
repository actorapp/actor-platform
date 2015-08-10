package im.actor.server.presences

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.time.{ Seconds, Span }

import im.actor.server.ActorSuite
import im.actor.server.db.DbExtension

class PresenceManagerSpec extends ActorSuite {
  behavior of "PresenceManager"

  it should "subscribe to presences" in e1
  it should "send presence on subscription" in e2
  it should "deliver presence changes" in e3
  it should "change presence to Offline after timeout" in e4

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
    presenceSetOnline(userId, 500)
    val lastSeenAt = probe.expectMsgPF() {
      case PresenceState(1, Online, Some(ls)) ⇒
        ls
    }

    presenceSetOffline(userId, 100)
    probe.expectMsgPF() {
      case PresenceState(1, Offline, Some(ls)) ⇒
        ls should ===(lastSeenAt)
    }
  }

  def e4() = {
    presenceSetOnline(userId, 100)
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
}
