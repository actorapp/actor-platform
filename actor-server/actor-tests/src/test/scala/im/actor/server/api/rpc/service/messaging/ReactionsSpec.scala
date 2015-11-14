package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.{ ApiMessageReaction, ResponseLoadHistory, ResponseReactionsResponse }
import im.actor.api.rpc._
import im.actor.server.{ ImplicitSessionRegion, ImplicitAuthService, MessagingSpecHelpers, BaseAppSuite }

final class ReactionsSpec
  extends BaseAppSuite
  with MessagingSpecHelpers
  with ImplicitAuthService
  with ImplicitSessionRegion {
  "SetReactions" should "set reactions" in set

  private implicit lazy val service = MessagingServiceImpl()

  def set() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    val aliceClient = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid)))

    val randomId = {
      implicit val clientData = aliceClient
      sendMessageToUser(bob.id, textMessage("Hi bob, do you like me?"))
    }

    {
      implicit val clientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid)))
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
      whenReady(service.handleLoadHistory(peer, 0, Int.MaxValue)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(history, _)) ⇒
            history.head.reactions should be(Vector(ApiMessageReaction(Vector(bob.id), "like")))
        }
      }
    }
  }
}