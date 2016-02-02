package im.actor.server.dialog

import im.actor.api.rpc._
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server._

final class GlobalCountersSpec
  extends BaseAppSuite
  with ImplicitAuthService
  with ImplicitSessionRegion
  with GroupsServiceHelpers
  with SeqUpdateMatchers {
  "Global counter" should "count unread messages in public groups" in publicGroups

  private implicit lazy val msgService = MessagingServiceImpl()
  private implicit lazy val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))

  def publicGroups(): Unit = {

    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid)))

    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid)))

    val groupPeer = {
      implicit val clientData = bobClientData
      createPubGroup("Public", "", Set(alice.id)).groupPeer
    }

    {
      implicit val clientData = bobClientData
      for (i ← 1 to 10) {
        sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello $i"))
      }
    }

    {
      implicit val clientData = aliceClientData

      //make sure that own messages does not count
      for (i ← 1 to 15) {
        sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello back $i"))
      }

      expectUpdate(classOf[UpdateCountersChanged]) { upd ⇒
        //2 initializing messages and 10 messages from bob
        upd.counters.globalCounter shouldEqual Some(12)
      }
    }
  }

}