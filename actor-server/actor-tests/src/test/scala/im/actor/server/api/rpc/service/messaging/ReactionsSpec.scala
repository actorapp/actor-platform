package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.{ UpdateReactionsUpdate, ApiMessageReaction, ResponseLoadHistory, ResponseReactionsResponse }
import im.actor.api.rpc._
import im.actor.api.rpc.peers.{ ApiPeerType, ApiOutPeer }
import im.actor.server._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }

final class ReactionsSpec
  extends BaseAppSuite
  with MessagingSpecHelpers
  with GroupsServiceHelpers
  with SeqUpdateMatchers
  with ImplicitAuthService
  with ImplicitSessionRegion {
  "SetReactions" should "set reactions in private" in setInPrivate
  it should "set reactions in group" in setInGroup

  private implicit lazy val service = MessagingServiceImpl()
  private implicit lazy val groupService = new GroupsServiceImpl(GroupInviteConfig(""))

  def setInPrivate() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClient = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    val randomId = {
      implicit val clientData = aliceClient
      sendMessageToUser(bob.id, textMessage("Hi bob, do you like me?"))
    }

    {
      implicit val clientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))
      val peer = getOutPeer(alice.id, bobAuthId)
      whenReady(service.handleMessageSetReaction(peer, randomId, "like")) { resp ⇒
        inside(resp) {
          case Ok(ResponseReactionsResponse(_, _, reactions)) ⇒
            reactions.size should ===(1)
            reactions.head.users should ===(Seq(bob.id))
            reactions.head.code should ===("like")
        }
      }
    }

    {
      implicit val clientData = aliceClient
      val peer = getOutPeer(bob.id, aliceAuthId)
      whenReady(service.handleLoadHistory(peer, 0, None, Int.MaxValue, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(history, _, _, _, _)) ⇒
            history.head.reactions should be(Vector(ApiMessageReaction(Vector(bob.id), "like")))
        }
      }
    }
  }

  def setInGroup() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClient = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClient = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid, 42)))

    val (peer, randomId) = {
      implicit val clientData = aliceClient
      val group = createGroup("Alice and Bob", Set(bob.id))
      val randomId = sendMessageToGroup(group.groupPeer.groupId, textMessage("Do you like me?"))
      (ApiOutPeer(ApiPeerType.Group, group.groupPeer.groupId, group.groupPeer.accessHash), randomId)
    }

    {
      implicit val clientData = bobClient
      whenReady(service.handleMessageSetReaction(peer, randomId, "like")) { resp ⇒
        inside(resp) {
          case Ok(ResponseReactionsResponse(_, _, reactions)) ⇒
            reactions.size should ===(1)
            reactions.head.users should ===(Seq(bob.id))
            reactions.head.code should ===("like")
        }
      }
    }

    {
      implicit val clientData = aliceClient
      expectUpdate(classOf[UpdateReactionsUpdate]) { upd ⇒
        upd.reactions.head.users should ===(Seq(bob.id))
      }
    }
  }
}