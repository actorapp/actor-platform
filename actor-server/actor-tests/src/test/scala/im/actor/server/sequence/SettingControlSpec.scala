package im.actor.server.sequence

import akka.actor.{ Props, Actor }
import akka.pattern.ask
import im.actor.api.rpc.PeersImplicits
import im.actor.concurrent.StashingActor
import im.actor.server.db.DbExtension
import im.actor.server.model.Peer
import im.actor.server.model.configs.Parameter
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.{ ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import im.actor.server.api.rpc.service.configs.ConfigsServiceImpl

class SettingControlSpec extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with PeersImplicits {

  behavior of "Setting Control"

  "Setting" should "up to date setting" in settings()

  private val configService = new ConfigsServiceImpl

  def settings() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val sessionId = createSessionId()

    val groupId = 1752533455
    val param = Parameter(alice.id, s"category.mobile.notification.chat.GROUP_${groupId}.enabled", Some("false"))
    whenReady(DbExtension(system).db.run(ParameterRepo.createOrUpdate(param)))(identity)

    val wrapperRef = system.actorOf(SettingControlWrapper.props(alice.id), "settingsWrapper")

    whenReady(wrapperRef.ask(GetSettings).mapTo[AllNotificationSettings]) { settings ⇒
      val peersSpecific = settings.specific("mobile").peers
      peersSpecific should have size 1
      peersSpecific.get(Peer.group(groupId)) shouldEqual Some(false)
    }
  }

  case object GetSettings

  object SettingControlWrapper {
    def props(userId: Int) = Props(new SettingControlWrapper(userId))
  }

  private final class SettingControlWrapper(userId: Int) extends Actor with StashingActor {

    private val settingsControl = context.actorOf(SettingsControl.props(userId), "test-settings")
    private var settings: AllNotificationSettings = _

    def receive: Receive = receiveStashing(replyTo ⇒ {
      case s: AllNotificationSettings ⇒
        settings = s
        context become initialized
        unstashAll()
    })

    def initialized: Receive = {
      case s: AllNotificationSettings ⇒ settings = s
      case GetSettings                ⇒ sender() ! settings
    }

  }
}
