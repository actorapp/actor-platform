package im.actor.server.http

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.{ FromRequestUnmarshaller, Unmarshal, Unmarshaller }
import akka.stream.Materializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.{ AuthData, ClientData, PeersImplicits }
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server._
import im.actor.server.api.http.json.Text
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.api.rpc.service.messaging.{ CommandParser, ReverseHooksListener }
import im.actor.server.group.{ GroupExtension, GroupServiceMessages }
import im.actor.server.migrations.IntegrationTokenMigrator
import im.actor.server.webhooks.http.routes.WebhooksHttpHandler
import play.api.libs.json.Json
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.ExecutionContext

class WebhookHandlerSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with PeersImplicits
  with ImplicitSequenceService
  with ImplicitSessionRegion
  with ImplicitAuthService
  with SeqUpdateMatchers {

  behavior of "WebhookHandler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  "Integration Token Migrator" should "migrate integration tokens to key value" in t.tokenMigration()

  "Reverse hooks listener" should "forward text messages in group to registered webhook" in t.reverseHooks()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
  implicit val messagingService = messaging.MessagingServiceImpl()
  private val groupExt = GroupExtension(system)

  object t {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    def createGroupAndBot() = {
      val groupOutPeer = createGroup("Bot test group", Set(user2.id)).groupPeer

      whenReady(db.run(persist.GroupBotRepo.findByGroup(groupOutPeer.groupId))) { optBot ⇒
        optBot shouldBe defined
        val bot = optBot.get
        bot.groupId shouldEqual groupOutPeer.groupId
      }
    }

    def sendInGroup() = {
      val handler = new WebhooksHttpHandler()

      val groupResponse = createGroup("Bot test group", Set(user2.id))
      val groupOutPeer = groupResponse.groupPeer
      val initSeq = groupResponse.seq
      val initState = groupResponse.state

      Thread.sleep(1000)

      val token = whenReady(groupExt.getIntegrationToken(groupOutPeer.groupId, user1.id)) { optToken ⇒
        optToken shouldBe defined
        optToken.get
      }

      val firstMessage = Text("Alert! All tests are failed!")
      whenReady(handler.send(firstMessage, token)) { _ ⇒
        expectUpdate(initSeq, classOf[UpdateMessage]) { upd ⇒
          upd.message shouldEqual ApiTextMessage(firstMessage.text, Vector.empty, None)
        }
        expectUpdate(initSeq, classOf[UpdateCountersChanged])(identity)
      }

      val (seq1, state1) = whenReady(sequenceService.handleGetState(Vector.empty)) { resp ⇒
        val ResponseSeq(seq, state) = resp.toOption.get
        (seq, state)
      }

      val secondMessage = Text("It's ok now!")
      whenReady(handler.send(secondMessage, token)) { _ ⇒
        expectUpdate(seq1, classOf[UpdateMessage]) { upd ⇒
          upd.message shouldEqual ApiTextMessage(secondMessage.text, Vector.empty, None)
        }
        expectUpdate(seq1, classOf[UpdateCountersChanged])(identity)
      }
    }

    def tokenMigration() = {
      val groups = for (i ← 1 to 10) yield {
        createGroup(s"$i", Set(user2.id)).groupPeer
      }

      IntegrationTokenMigrator.migrate()

      val kv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

      groups foreach { group ⇒
        val token = whenReady(groupExt.getIntegrationToken(group.groupId, user1.id)) { optToken ⇒
          optToken shouldBe defined
          optToken.get
        }
        whenReady(kv.get(token)) { optGroupId ⇒
          optGroupId shouldBe defined
          optGroupId shouldEqual Some(group.groupId)
        }
      }
    }

    def reverseHooks() = {
      val handler = new WebhooksHttpHandler()

      val hook3000 = new DummyHookListener(3000)
      val hook4000 = new DummyHookListener(4000)

      val group = createGroup(s"Reverse hooks group", Set(user2.id)).groupPeer

      ReverseHooksListener.startSingleton()

      val token = whenReady(groupExt.getIntegrationToken(group.groupId)) { optToken ⇒
        optToken shouldBe defined
        optToken.get
      }

      whenReady(handler.register(token, "http://localhost:3000"))(_.isRight shouldBe true)
      whenReady(handler.register(token, "http://localhost:4000"))(_.isRight shouldBe true)

      Thread.sleep(4000)

      val sendText = List("/task jump", "/task eat", "/command sleep", "/command die")

      object Parser extends CommandParser
      val commands = sendText map Parser.parseCommand

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 1L, ApiTextMessage(sendText.head, Vector.empty, None), None, None))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 2L, GroupServiceMessages.changedTitle("xx"), None, None))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 3L, ApiTextMessage(sendText(1), Vector.empty, None), None, None))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 4L, ApiJsonMessage("Some info"), None, None))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 5L, ApiTextMessage(sendText(2), Vector.empty, None), None, None))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 6L, ApiDocumentMessage(1L, 2L, 1, "", "", None, None), None, None))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 7L, ApiTextMessage(sendText(3), Vector.empty, None), None, None))(_ ⇒ ())
      Thread.sleep(4000)

      val messages3000 = hook3000.getMessages
      messages3000 should have size 4
      messages3000.map(m ⇒ Some(m.command → m.text)) should contain theSameElementsAs commands

      val messages4000 = hook4000.getMessages
      messages4000 should have size 4
      messages4000.map(m ⇒ Some(m.command → m.text)) should contain theSameElementsAs commands
    }
  }

  final class DummyHookListener(port: Int)(implicit system: ActorSystem, materializer: Materializer) extends PlayJsonSupport {

    import akka.http.scaladsl.Http
    import akka.http.scaladsl.server.Directives._
    import akka.http.scaladsl.server.Route
    import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
    import im.actor.server.api.rpc.service.messaging.ReverseHooksWorker._

    implicit val ec: ExecutionContext = system.dispatcher
    implicit val toMessage: FromRequestUnmarshaller[MessageToWebhook] = Unmarshaller { implicit ec ⇒ req ⇒
      Unmarshal(req.entity).to[String].map { body ⇒
        Json.parse(body).as[MessageToWebhook]
      }
    }

    private var messages = scala.collection.mutable.Set.empty[MessageToWebhook]

    def getMessages = messages

    def clean() = messages = scala.collection.mutable.Set.empty[MessageToWebhook]

    private def routes: Route =
      post {
        entity(as[List[MessageToWebhook]]) { received ⇒
          received should have length 1
          messages += received.head
          complete("{}")
        }
      }

    Http().bind("0.0.0.0", port).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }

}
