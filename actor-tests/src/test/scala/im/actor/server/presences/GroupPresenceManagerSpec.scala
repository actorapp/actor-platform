package im.actor.server.presences

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.time.{ Seconds, Span }

import im.actor.server.SqlSpecHelpers
import im.actor.util.testing.ActorSuite

class GroupPresenceManagerSpec extends ActorSuite with SqlSpecHelpers {
  behavior of "GroupPresenceManager"

  it should "subscribe/unsubscribe to group presences" in e1
  //it should "send presence on subscription" in e2
  //it should "deliver presence changes" in e3
  //it should "change presence to Offline after timeout" in e4

  import GroupPresenceManager._

  implicit val ec: ExecutionContext = system.dispatcher
  implicit lazy val (ds, db) = migrateAndInitDb()

  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  implicit val timeout: Timeout = Timeout(5.seconds)

  implicit val userPresenceRegion = PresenceManager.startRegion()
  implicit val region = GroupPresenceManager.startRegion()

  val probe = TestProbe()
  val userId = 1
  val groupId = 100

  def e1() = {
    whenReady(subscribe(groupId, probe.ref)) { _ ⇒ }
    GroupPresenceManager.notifyGroupUserAdded(groupId, userId)

    PresenceManager.presenceSetOnline(userId, 1000)

    probe.expectMsgPF() {
      case GroupPresenceState(100, 1) ⇒
    }

    probe.expectMsgPF() {
      case GroupPresenceState(100, 0) ⇒
    }

    whenReady(unsubscribe(groupId, probe.ref)) { _ ⇒ }
    probe.expectNoMsg()
  }
  /*
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
  }*/

  override def afterAll: Unit = {
    super.afterAll()
    system.awaitTermination()
    ds.close()
  }
}

