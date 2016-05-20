package im.actor.server.api.rpc.service.eventbus

import akka.testkit.TestProbe
import im.actor.api.rpc.eventbus._
import im.actor.server.mtproto.protocol.SessionHello
import im.actor.server.session.SessionSpecHelpers
import im.actor.server.{ SeqUpdateMatchers, ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import im.actor.api.rpc._

import scala.util.Random

final class EventbusServiceSpec
  extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with SeqUpdateMatchers
  with SessionSpecHelpers {
  it should "broadcast messages" in broadcast

  lazy val service = new EventbusServiceImpl(system)

  def broadcast() = {
    val aliceProbe = TestProbe()
    val bobProbe = TestProbe()

    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val aliceSessionId = 1L
    val bobSessionId = 2L

    val aliceClientData = ClientData(aliceAuthId, aliceSessionId, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, bobSessionId, Some(AuthData(bob.id, bobAuthSid, 42)))

    sendMessageBox(aliceAuthId, aliceSessionId, sessionRegion.ref, Random.nextLong(), SessionHello)(aliceProbe)
    ignoreNewSession()(aliceProbe)
    expectMessageAck()(aliceProbe)

    val (id, aliceDeviceId) = {
      implicit val clientData = aliceClientData

      whenReady(service.handleCreateNewEventBus(Some(1000), Some(true))) {
        case Ok(ResponseCreateNewEventBus(id, deviceId)) ⇒ (id, deviceId)
      }
    }

    {
      implicit val clientData = bobClientData

      whenReady(service.handleJoinEventBus(id, None)) {
        case Ok(_) ⇒
      }

      service.handlePostToEventBus(id, Vector(aliceDeviceId), Array[Byte](123))
    }

    expectWeakUpdate(aliceAuthId, aliceSessionId)(aliceProbe).updateHeader shouldBe UpdateEventBusDeviceConnected.header
    expectWeakUpdate(aliceAuthId, aliceSessionId)(aliceProbe).updateHeader shouldBe UpdateEventBusMessage.header

    Thread.sleep(1000)

    expectWeakUpdate(aliceAuthId, aliceSessionId)(aliceProbe).updateHeader shouldBe UpdateEventBusDisposed.header

    {
      implicit val clientData = aliceClientData

      whenReady(service.handleKeepAliveEventBus(id, Some(1000))) {
        case Error(_) ⇒
      }
    }
  }
}