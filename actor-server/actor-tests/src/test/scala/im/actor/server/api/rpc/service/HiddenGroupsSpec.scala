package im.actor.server.api.rpc.service

import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiTextMessage, ResponseLoadDialogs }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiOutPeer }
import im.actor.server.{ ImplicitSessionRegionProxy, ImplicitAuthService, BaseAppSuite }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.group.{ GroupType, GroupExtension }
import org.scalatest.Inside._
import im.actor.server.persist

import scala.util.Random

final class HiddenGroupsSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegionProxy {
  "LoadDialogs" should "not load hidden groups" in loadHidden

  private val groupExt = GroupExtension(system)
  private val service = MessagingServiceImpl()

  def loadHidden() = {
    val (user, authId, authSid, _) = createUser()
    implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid)))

    val groupId = 1

    whenReady(for {
      group ← groupExt.createInternal(groupId, GroupType.General, user.id, "hidden", Set.empty, isHidden = true, isHistoryShared = true)
      _ ← service.handleSendMessage(ApiOutPeer(ApiPeerType.Group, groupId, group.accessHash), Random.nextLong, ApiTextMessage("Hi there", Vector.empty, None))
    } yield ()) { _ ⇒
      whenReady(service.handleLoadDialogs(0, Int.MaxValue)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadDialogs(dialogs, _, _)) ⇒
            dialogs.length shouldBe (0)

            db.run(persist.HistoryMessageRepo.getUnreadTotal(user.id))
        }
      }
    }
  }
}