package im.actor.server.dialog

import im.actor.api.rpc.messaging.ResponseLoadDialogs
import im.actor.api.rpc._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.{ GroupsServiceHelpers, ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl

final class UnreadCountersSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion with GroupsServiceHelpers {
  it should "consider own messages read" in ownMessagesRead

  it should "display correct unread count in public groups" in publicGroups

  private implicit lazy val msgService = MessagingServiceImpl()
  private implicit lazy val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))

  def ownMessagesRead(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))

      sendMessageToUser(alice.id, textMessage("Hi Alice!"))
    }

    {
      implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

      sendMessageToUser(bob.id, textMessage("Hi Bob!"))

      Thread.sleep(1000)

      whenReady(msgService.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadDialogs(_, _, dialogs, _, _)) ⇒
            dialogs.head.unreadCount should ===(1)
        }
      }
    }
  }

  def publicGroups(): Unit = {

    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    implicit val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val groupPeer = createPubGroup("Public", "", Set(alice.id)).groupPeer

    for (i ← 1 to 10) {
      sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello $i"))
    }

    Thread.sleep(1000)

    whenReady(msgService.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponseLoadDialogs(_, _, dialogs, _, _)) ⇒
          dialogs.head.unreadCount should ===(0)
      }
    }
  }

}