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

  it should "not count messages in public group, after user been kicked from it" in publicGroupsAfterKick

  private implicit lazy val msgService = MessagingServiceImpl()
  private implicit lazy val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))

  def publicGroups(): Unit = {

    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

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

      //make sure that own messages don't count
      sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello back"))

      expectUpdate(classOf[UpdateCountersChanged]) { upd ⇒
        //2 initializing messages and 10 messages from bob
        upd.counters.globalCounter shouldEqual Some(12)
      }
    }
  }

  def publicGroupsAfterKick(): Unit = {

    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    val groupPeer = {
      implicit val clientData = bobClientData
      createPubGroup("Public", "", Set(alice.id)).groupPeer
    }

    {
      implicit val clientData = bobClientData
      // Ten messages that used will see, and they will count in global counter
      for (i ← 1 to 10) {
        sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello $i"))
      }

      // Global counter for kicked user should go to zero
      whenReady(groupsService.handleKickUser(groupPeer, 12L, getUserOutPeer(alice.id, aliceAuthId), Vector.empty))(identity)

      // These messages should not go to counter
      for (i ← 1 to 10) {
        sendMessageToGroup(groupPeer.groupId, textMessage(s"Hello kicked user $i"))
      }
    }

    val aliceSeq = {
      implicit val clientData = bobClientData
      val seq = getCurrentSeq(aliceClientData)
      // This is only message that should go to counter
      sendMessageToUser(alice.id, textMessage(s"Hi Alice"))
      seq
    }

    {
      implicit val clientData = aliceClientData

      expectUpdate(aliceSeq, classOf[UpdateCountersChanged]) { upd ⇒
        // currently returns 11 instead of 1
        upd.counters.globalCounter shouldEqual Some(1)
      }
    }
  }

}