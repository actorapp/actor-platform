package im.actor.server.dialog

import im.actor.api.rpc.messaging.ResponseLoadDialogs
import im.actor.api.rpc._
import im.actor.server.{ ImplicitSessionRegion, ImplicitAuthService, BaseAppSuite }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl

final class UnreadCountersSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion {
  it should "consider own messages read" in ownMessagesRead

  private implicit lazy val msgService = MessagingServiceImpl()

  def ownMessagesRead(): Unit = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()

    {
      implicit val clientData = ClientData(bobAuthId, 2, Some(AuthData(bob.id, bobAuthSid)))

      sendMessageToUser(alice.id, textMessage("Hi Alice!"))
    }

    {
      implicit val clientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid)))

      sendMessageToUser(bob.id, textMessage("Hi Bob!"))

      Thread.sleep(1000)

      whenReady(msgService.handleLoadDialogs(0, 100)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadDialogs(_, _, dialogs)) ⇒
            dialogs.head.unreadCount should ===(1)
        }
      }
    }
  }
}