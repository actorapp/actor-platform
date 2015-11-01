package im.actor.server.bot

import akka.actor.{ Props, ActorSystem }
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.messaging.{ ResponseLoadHistory, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType, ApiPeer }
import im.actor.api.rpc._
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.dialog.DialogExtension
import im.actor.server._
import org.scalatest.Inside._

import scala.util.Random

object EchoBot {
  val UserId = 100

  def start()(implicit system: ActorSystem) = system.actorOf(props, "EchoBot")

  private def props = Props(classOf[EchoBot])
}

final class EchoBot extends InternalBot(EchoBot.UserId, "echo", "Echo Bot", isAdmin = false) {
  import im.actor.bots.BotMessages._

  override def onMessage(m: Message): Unit = {
    m.message match {
      case TextMessage(text) ⇒
        requestSendMessage(m.peer, nextRandomId(), TextMessage(text))
      case _ ⇒
    }
  }

  override def onRawUpdate(u: RawUpdate): Unit = {}
}

final class EchoBotSpec
  extends BaseAppSuite
  with ServiceSpecHelpers
  with GroupsServiceHelpers
  with ImplicitAuthService
  with ImplicitSessionRegionProxy {
  it should "reply with the same message (private)" in replyPrivate
  it should "reply with the same message (group)" in replyGroup

  private lazy val dialogExt = DialogExtension(system)
  private lazy val msgService = MessagingServiceImpl()
  private implicit lazy val groupsService = new GroupsServiceImpl(GroupInviteConfig(""))

  def replyPrivate() = {
    val (user, authId, _) = createUser()

    Thread.sleep(1000)

    whenReady(dialogExt.sendMessage(
      peer = ApiPeer(ApiPeerType.Private, EchoBot.UserId),
      senderUserId = user.id,
      senderAuthId = authId,
      randomId = Random.nextLong(),
      message = ApiTextMessage("Hello", Vector.empty, None),
      isFat = false
    ))(identity)

    Thread.sleep(2000)

    implicit val clientData = ClientData(authId, Random.nextLong(), Some(user.id))

    val botOutPeer = getOutPeer(EchoBot.UserId, authId)

    whenReady(msgService.handleLoadHistory(botOutPeer, 0, 100)) { rsp ⇒
      inside(rsp) {
        case Ok(ResponseLoadHistory(history, _)) ⇒
          history.length shouldBe (2)
          val tm = history.last.message.asInstanceOf[ApiTextMessage]
          tm.text shouldBe "Hello"
      }
    }
  }

  def replyGroup() = {
    val (user, authId, _) = createUser()
    implicit val clientData = ClientData(authId, 1, Some(user.id))
    val groupPeer = createGroup("Echo group", Set(EchoBot.UserId)).groupPeer
    val outPeer = ApiOutPeer(ApiPeerType.Group, groupPeer.groupId, groupPeer.accessHash)

    whenReady(dialogExt.sendMessage(
      peer = ApiPeer(ApiPeerType.Group, groupPeer.groupId),
      senderUserId = user.id,
      senderAuthId = authId,
      randomId = Random.nextLong(),
      message = ApiTextMessage("Hello", Vector.empty, None),
      isFat = false
    ))(identity)

    Thread.sleep(2000)

    whenReady(msgService.handleLoadHistory(outPeer, 0, 100)) { rsp ⇒
      inside(rsp) {
        case Ok(ResponseLoadHistory(history, _)) ⇒
          history.length shouldBe 4
          val tm = history.last.message.asInstanceOf[ApiTextMessage]
          tm.text shouldBe "Hello"
      }
    }
  }

  override def beforeAll = {
    EchoBot.start()
  }
}