package im.actor.server.api.rpc.service.eventbus

import im.actor.api.rpc.eventbus.{ApiEventBusDestination, ResponseCreateNewEventBus}
import im.actor.server.{SeqUpdateMatchers, ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite}
import im.actor.api.rpc._

final class EventbusServiceSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion with SeqUpdateMatchers {
  it should "broadcast messages" in broadcast

  lazy val service = new EventbusServiceImpl(system)

  def broadcast() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClientData = ClientData(aliceAuthId, 1L, Some(AuthData(alice.id, aliceAuthSid)))
    val bobClientData = ClientData(bobAuthId, 1L, Some(AuthData(bob.id, bobAuthSid)))

    val (id, aliceDeviceId) = {
      implicit val clientData = aliceClientData

      whenReady(service.handleCreateNewEventBus(None, None)) {
        case Ok(ResponseCreateNewEventBus(id, deviceId)) => (id, deviceId)
      }
    }

    {
      implicit val clientData = bobClientData

      whenReady(service.handleJoinEventBus(id, None)) {
        case Ok(_) =>
      }

      service.handlePostToEventBus(id, Vector(ApiEventBusDestination(alice.id, Vector(aliceDeviceId))), Array[Byte](123))
    }
  }
}