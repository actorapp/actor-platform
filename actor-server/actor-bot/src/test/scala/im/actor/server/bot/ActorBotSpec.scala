package im.actor.server.bot

import im.actor.api.rpc.messaging.{ ResponseLoadHistory, ApiTextMessage }
import im.actor.api.rpc._
import im.actor.server._
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.dialog.DialogExtension
import org.scalatest.Inside._

import scala.util.Random

final class ActorBotSpec
  extends BaseAppSuite
  with ServiceSpecHelpers
  with ImplicitAuthService
  with ImplicitSessionRegionProxy {
  it should "receive messages" in rcv

  private lazy val dialogExt = DialogExtension(system)
  private lazy val msgService = MessagingServiceImpl()

  def rcv() = {
    val (user, authId, _) = createUser()

    ActorBot.start()

    Thread.sleep(1000)

    whenReady(dialogExt.sendMessage(
      peer = ActorBot.ApiPeer,
      senderUserId = user.id,
      senderAuthId = authId,
      randomId = Random.nextLong(),
      message = ApiTextMessage("/bot new mybot MyBotName", Vector.empty, None),
      isFat = false
    ))(identity)

    Thread.sleep(1000)

    implicit val clientData = ClientData(authId, Random.nextLong(), Some(user.id))

    val botOutPeer = getOutPeer(ActorBot.UserId, authId)

    whenReady(msgService.handleLoadHistory(botOutPeer, 0, 100)) { rsp ⇒
      inside(rsp) {
        case Ok(ResponseLoadHistory(history, _)) ⇒
          history.length shouldBe (2)
          val tm = history.last.message.asInstanceOf[ApiTextMessage]
          tm.text.startsWith("Yay!") shouldBe (true)
      }
    }
  }
}
