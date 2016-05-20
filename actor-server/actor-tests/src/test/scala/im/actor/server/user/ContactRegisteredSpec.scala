package im.actor.server.user

import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.messaging._
import im.actor.api.rpc._
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.persist.contact.UnregisteredEmailContactRepo
import im.actor.server._

import scala.util.Random

final class ContactRegisteredSpec extends BaseAppSuite with ImplicitAuthService with ImplicitSessionRegion with MessagingSpecHelpers with SeqUpdateMatchers {
  it should "notify ContactRegistered" in notifyContactRegistered()
  it should "not create dialog with contacts which receive ContactRegistered" in notCreateDialog()

  private lazy val msgService = MessagingServiceImpl()

  def notifyContactRegistered() = {
    val (alice, bob) = createUserRegisterContact()
    implicit val clientData = alice

    expectUpdate(classOf[UpdateContactRegistered])(_ ⇒ ())

    Thread.sleep(300)

    whenReady(msgService.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponseLoadDialogs(_, _, Vector(dialog), _, _)) ⇒
          dialog.peer should ===(ApiPeer(ApiPeerType.Private, bob.authData.get.userId))

          inside(dialog.message) {
            case ApiServiceMessage(_, Some(ApiServiceExContactRegistered(userId))) ⇒
              userId should ===(bob.authData.get.userId)
          }
      }
    }

    whenReady(msgService.handleLoadHistory(getOutPeer(bob.authData.get.userId, clientData.authId), 0, None, 100, Vector.empty)) { resp ⇒
      inside(resp) {
        case Ok(ResponseLoadHistory(Vector(hm), _, _, _, _)) ⇒
          inside(hm.message) {
            case ApiServiceMessage(_, Some(ApiServiceExContactRegistered(userId))) ⇒
              userId should ===(bob.authData.get.userId)
          }
      }
    }
  }

  def notCreateDialog() = {
    val (alice, bob) = createUserRegisterContact()

    {
      implicit val clientData = alice

      whenReady(msgService.handleMessageRead(getOutPeer(bob.optUserId.get, alice.authId), Int.MaxValue.toLong))(identity)
    }

    Thread.sleep(1000)

    {
      implicit val clientData = bob

      whenReady(msgService.handleLoadDialogs(0, 100, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadDialogs(_, _, Vector(), _, _)) ⇒
        }
      }

      whenReady(msgService.handleLoadGroupedDialogs(Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadGroupedDialogs(Vector(dgp, dgg), _, _, _, _, _, _)) ⇒
            dgp.dialogs should ===(Vector())
            dgg.dialogs should ===(Vector())
        }
      }

      whenReady(msgService.handleLoadHistory(getOutPeer(alice.authData.get.userId, bob.authId), 0, None, 100, Vector.empty)) { resp ⇒
        inside(resp) {
          case Ok(ResponseLoadHistory(Vector(), Vector(), _, _, _)) ⇒
        }
      }
    }
  }

  private def createUserRegisterContact(): (ClientData, ClientData) = {
    val (alice, aliceAuthId, aliceAuthSid, _) = createUser()
    val aliceClientData = ClientData(aliceAuthId, 1, Some(AuthData(alice.id, aliceAuthSid, 42)))

    whenReady(db.run(UnregisteredEmailContactRepo.create("test@acme.com", alice.id, None)))(identity)

    val (bob, bobAuthId, bobAuthSid, _) = createUser()
    val bobClientData = ClientData(bobAuthId, 1, Some(AuthData(bob.id, bobAuthSid, 42)))

    whenReady(UserExtension(system).addEmail(bob.id, "test@acme.com"))(identity)

    (aliceClientData, bobClientData)
  }
}