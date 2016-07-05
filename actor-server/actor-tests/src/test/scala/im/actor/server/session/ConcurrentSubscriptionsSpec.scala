package im.actor.server.session

import akka.testkit.TestProbe
import im.actor.api.rpc.sequence.RequestSubscribeToOnline
import im.actor.concurrent.FutureExt
import im.actor.server.ServiceSpecHelpers
import im.actor.server.acl.ACLUtils
import org.scalatest.concurrent.Futures

import scala.concurrent.duration._
import scala.util.Random

final class ConcurrentSubscriptionsSpec extends BaseSessionSpec with ServiceSpecHelpers with Futures {
  behavior of "Session"

  it should "not stale on lots of concurrent subscriptions" in concurrentSubscriptions

  implicit val probe = TestProbe()

  def concurrentSubscriptions(): Unit = {
    val (_, authId, _, _) = createUser()

    val sessionId = Random.nextLong()

    val UsersNum = 100

    val peersFuture = FutureExt.ftraverse(1 to UsersNum map (_ ⇒ createUser()._1.id))(ACLUtils.getUserOutPeer(_, authId))

    whenReady(peersFuture) { peers ⇒
      peers.foreach { peer ⇒
        sendRequest(authId, sessionId, sessionRegion.ref, RequestSubscribeToOnline(Vector(peer)))
      }
    }

    val expectedMessagesNum = UsersNum * 2 + 1 // NewSession + acks + respones + presences

    probe.receiveN(expectedMessagesNum, 10.seconds)
  }
}
