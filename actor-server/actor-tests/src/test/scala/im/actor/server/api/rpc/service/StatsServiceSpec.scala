package im.actor.server.api.rpc.service

import im.actor.api.rpc.collections.{ ApiInt32Value, ApiMapValue, ApiMapValueItem, ApiStringValue }
import im.actor.api.rpc.stats.{ ApiAppVisibleChanged, ApiUntypedEvent }
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server.api.rpc.service.stats.StatsServiceImpl
import im.actor.server.persist.ClientStatsRepo
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import play.api.libs.json.Json

class StatsServiceSpec extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {

  behavior of "StatsService"

  it should "store events in database" in storeEvents()

  it should "store params as json" in storeParamsAsJson()

  val service = new StatsServiceImpl

  def storeEvents() = {
    val (user, userAuthId, userAuthSid, _) = createUser()
    implicit val aliceClientData = ClientData(userAuthId, 1, Some(AuthData(user.id, userAuthSid, 42)))

    whenReady(service.handleStoreEvents(Vector(
      ApiAppVisibleChanged(visible = true),
      ApiAppVisibleChanged(visible = false)
    )))(identity)

    whenReady(db.run(ClientStatsRepo.findByUserId(user.id))) { stats ⇒
      stats should have length 2
      stats foreach { s ⇒
        s.userId shouldEqual user.id
        s.authId shouldEqual userAuthId
        s.eventType shouldEqual "VisibleChanged"
      }
    }
  }

  def storeParamsAsJson() = {
    val (user, userAuthId, userAuthSid, _) = createUser()
    implicit val aliceClientData = ClientData(userAuthId, 1, Some(AuthData(user.id, userAuthSid, 42)))

    whenReady(service.handleStoreEvents(Vector(
      ApiUntypedEvent("AppCrash", Some(ApiMapValue(Vector(
        ApiMapValueItem("exception", ApiStringValue("NullPointerException")),
        ApiMapValueItem("line", ApiInt32Value(24))
      ))))
    )))(identity)

    whenReady(db.run(ClientStatsRepo.findByUserId(user.id))) { stats ⇒
      stats should have length 1
      val stat = stats.head
      stat.eventType shouldEqual "AppCrash"

      val parts = stat.event split ";"
      val json = Json.parse(parts(0))
      (json \ "exception").validate[String].asOpt shouldEqual Some("NullPointerException")
      (json \ "line").validate[Int].asOpt shouldEqual Some(24)
    }
  }

}
