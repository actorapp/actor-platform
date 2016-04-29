package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.ResponseLoadDialogs
import im.actor.api.rpc._
import im.actor.server._

final class LoadDialogsSpec
  extends BaseAppSuite
  with MessagingSpecHelpers
  with ImplicitMessagingService
  with ImplicitAuthService
  with ImplicitSessionRegion {
  it should "bump dialog on appearing or new message" in bump
  it should "not hide archived dialogs" in notHideArchived

  def bump() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val (eve, eveAuthId, eveAuthSid, _) = createUser()

    val aliceClientData = ClientData(aliceAuthId, 1L, Some(AuthData(alice.id, aliceAuthSid, 42)))
    val bobClientData = ClientData(bobAuthId, 1L, Some(AuthData(bob.id, bobAuthSid, 42)))
    val eveClientData = ClientData(eveAuthId, 1L, Some(AuthData(eve.id, eveAuthSid, 42)))

    {
      implicit val clientData = aliceClientData
      sendMessageToUser(bob.id, "Hi ")
    }

    {
      implicit val clientData = eveClientData
      sendMessageToUser(alice.id, "Privet")
    }

    {
      implicit val clientData = aliceClientData
      loadDialogs().map(_.peer.id) should be(Seq(eve.id, bob.id))
    }

    {
      implicit val clientData = bobClientData
      sendMessageToUser(alice.id, "Hola")
    }

    {
      implicit val clientData = aliceClientData
      loadDialogs().map(_.peer.id) should be(Seq(bob.id, eve.id))
    }
  }

  def notHideArchived() = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val bob = createUser()._1
    val bobPeer = getOutPeer(bob.id, aliceAuthId)

    implicit val clientData = ClientData(aliceAuthId, 1L, Some(AuthData(alice.id, aliceAuthSid, 42)))

    sendMessageToUser(bob.id, "Hi")

    whenReady(msgService.handleArchiveChat(bobPeer))(identity)

    whenReady(msgService.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponseLoadDialogs(_, _, dialogs, _, _)) ⇒
          dialogs.map(_.peer) should be(Seq(bobPeer.asPeer))
      }
    }
  }
}