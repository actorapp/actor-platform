package im.actor.server.bot

import im.actor.api.rpc.contacts.ResponseSearchContacts
import im.actor.api.rpc.messaging.{ ResponseLoadHistory, ApiTextMessage }
import im.actor.api.rpc._
import im.actor.server._
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.dialog.DialogExtension
import org.scalatest.Inside._

import scala.util.Random

final class ActorBotSpec
  extends BaseAppSuite
  with ServiceSpecHelpers
  with ImplicitAuthService
  with ImplicitSessionRegion {
  it should "create other bots" in rcv
  it should "report about taken username" in takenUsername // TODO: make it independent from rcv
  it should "be found by username" in username

  private lazy val dialogExt = DialogExtension(system)
  private lazy val msgService = MessagingServiceImpl()
  private lazy val contactsService = new ContactsServiceImpl

  ActorBot.start()

  def rcv() = {
    val (user, authId, authSid, _) = createUser()

    Thread.sleep(1000)

    whenReady(dialogExt.sendMessage(
      peer = ActorBot.ApiPeer,
      senderUserId = user.id,
      senderAuthSid = authSid,
      randomId = Random.nextLong(),
      message = ApiTextMessage("/bot new mybot MyBotName", Vector.empty, None),
      isFat = false
    ))(identity)

    Thread.sleep(2000)

    implicit val clientData = ClientData(authId, Random.nextLong(), Some(AuthData(user.id, authSid)))

    val botOutPeer = getOutPeer(ActorBot.UserId, authId)

    whenReady(msgService.handleLoadHistory(botOutPeer, 0, 100)) { rsp ⇒
      inside(rsp) {
        case Ok(ResponseLoadHistory(history, _)) ⇒
          history.length shouldBe 2
          val tm = history.last.message.asInstanceOf[ApiTextMessage]
          tm.text.startsWith("Yay!") shouldBe true
      }
    }
  }

  def takenUsername() = {
    val (user, authId, authSid, _) = createUser()

    Thread.sleep(1000)

    whenReady(dialogExt.sendMessage(
      peer = ActorBot.ApiPeer,
      senderUserId = user.id,
      senderAuthSid = authSid,
      randomId = Random.nextLong(),
      message = ApiTextMessage("/bot new mybot MyBotName", Vector.empty, None),
      isFat = false
    ))(identity)

    Thread.sleep(1000)

    implicit val clientData = ClientData(authId, Random.nextLong(), Some(AuthData(user.id, authSid)))

    val botOutPeer = getOutPeer(ActorBot.UserId, authId)

    whenReady(msgService.handleLoadHistory(botOutPeer, 0, 100)) { rsp ⇒
      inside(rsp) {
        case Ok(ResponseLoadHistory(history, _)) ⇒
          history.length shouldBe 2
          val tm = history.last.message.asInstanceOf[ApiTextMessage]
          tm.text shouldBe "Username already taken"
      }
    }
  }

  def username() = {
    val (user, authId, authSid, _) = createUser()

    implicit val clientData = ClientData(authId, Random.nextLong(), Some(AuthData(user.id, authSid)))

    whenReady(contactsService.handleSearchContacts("actor")) { resp ⇒
      inside(resp) {
        case Ok(ResponseSearchContacts(users)) ⇒
          users.length shouldBe 1
      }
    }
  }
}
