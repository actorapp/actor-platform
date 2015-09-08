package im.actor.server.http

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.{ Unmarshaller, Unmarshal, FromRequestUnmarshaller }
import akka.stream.Materializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.server._
import im.actor.server.api.http.json.Text
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging.{ CommandParser, ReverseHooksListener }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.group.{ GroupOffice, GroupServiceMessages }
import im.actor.server.migrations.IntegrationTokenMigrator
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import play.api.libs.json.Json
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.ExecutionContext

class WebhookHandlerSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with MessageParsing
  with PeersImplicits
  with ImplicitGroupRegions
  with ImplicitSequenceService
  with ImplicitSessionRegionProxy
  with ImplicitAuthService
  with SequenceMatchers {

  behavior of "WebhookHandler"

  it should "create group bot on group creation" in t.createGroupAndBot()

  it should "allow bot to send message to it's group" in t.sendInGroup()

  "Integration Token Migrator" should "migrate integration tokens to key value" in t.tokenMigration()

  "Reverse hooks listener" should "forward text messages in group to registered webhook" in t.reverseHooks()

  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")
  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)
  implicit val messagingService = messaging.MessagingServiceImpl(mediator)

  object t {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    def createGroupAndBot() = {
      val groupOutPeer = createGroup("Bot test group", Set(user2.id)).groupPeer

      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { optBot ⇒
        optBot shouldBe defined
        val bot = optBot.get
        bot.groupId shouldEqual groupOutPeer.groupId
      }
    }

    def sendInGroup() = {
      val handler = new WebhooksHandler()

      val groupResponse = createGroup("Bot test group", Set(user2.id))
      val groupOutPeer = groupResponse.groupPeer
      val initSeq = groupResponse.seq
      val initState = groupResponse.state

      Thread.sleep(1000)

      val token = whenReady(GroupOffice.getIntegrationToken(groupOutPeer.groupId, user1.id)) { optToken ⇒
        optToken shouldBe defined
        optToken.get
      }

      val firstMessage = Text("Alert! All tests are failed!")
      whenReady(handler.send(firstMessage, token)) { _ ⇒
        expectUpdatesUnordered(ignoreUnmatched)(initSeq, initState, Seq(UpdateMessage.header, UpdateCountersChanged.header)) {
          case (UpdateMessage.header, u) ⇒
            val update = parseUpdate[UpdateMessage](u)
            update.message shouldEqual ApiTextMessage(firstMessage.text, Vector.empty, None)
          case (UpdateCountersChanged.header, update) ⇒ parseUpdate[UpdateCountersChanged](update)
        }
      }

      val (seq1, state1) = whenReady(sequenceService.handleGetState()) { resp ⇒
        val ResponseSeq(seq, state) = resp.toOption.get
        (seq, state)
      }

      val secondMessage = Text("It's ok now!")
      whenReady(handler.send(secondMessage, token)) { _ ⇒
        expectUpdatesUnordered(failUnmatched)(seq1, state1, Seq(UpdateMessage.header, UpdateCountersChanged.header)) {
          case (UpdateMessage.header, u) ⇒
            val update = parseUpdate[UpdateMessage](u)
            update.message shouldEqual ApiTextMessage(secondMessage.text, Vector.empty, None)
          case (UpdateCountersChanged.header, update) ⇒ parseUpdate[UpdateCountersChanged](update)
        }
      }
    }

    def tokenMigration() = {
      val groups = for (i ← 1 to 300) yield createGroup(s"$i", Set(user2.id)).groupPeer

      IntegrationTokenMigrator.migrate()

      val kv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

      groups foreach { group ⇒
        val token = whenReady(GroupOffice.getIntegrationToken(group.groupId, user1.id)) { optToken ⇒
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
      val handler = new WebhooksHandler()

      val hook3000 = new DummyHookListener(3000)
      val hook4000 = new DummyHookListener(4000)

      val group = createGroup(s"Reverse hooks group", Set(user2.id)).groupPeer

      ReverseHooksListener.startSingleton(mediator)

      val token = whenReady(GroupOffice.getIntegrationToken(group.groupId)) { optToken ⇒
        optToken shouldBe defined
        optToken.get
      }

      whenReady(handler.register(token, "http://localhost:3000"))(_.isRight shouldBe true)
      whenReady(handler.register(token, "http://localhost:4000"))(_.isRight shouldBe true)

      Thread.sleep(4000)

      val sendText = List("/task jump", "/task eat", "/command sleep", "/command die")

      object Parser extends CommandParser
      val commands = (sendText map Parser.parseCommand)

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 1L, ApiTextMessage(sendText(0), Vector.empty, None)))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 2L, GroupServiceMessages.changedTitle("xx")))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 3L, ApiTextMessage(sendText(1), Vector.empty, None)))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 4L, ApiJsonMessage("Some info")))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 5L, ApiTextMessage(sendText(2), Vector.empty, None)))(_ ⇒ ())
      whenReady(messagingService.handleSendMessage(group.asOutPeer, 6L, ApiDocumentMessage(1L, 2L, 1, "", "", None, None)))(_ ⇒ ())

      whenReady(messagingService.handleSendMessage(group.asOutPeer, 7L, ApiTextMessage(sendText(3), Vector.empty, None)))(_ ⇒ ())
      Thread.sleep(4000)

      val messages3000 = hook3000.getMessages
      messages3000 should have size 4
      messages3000.map(m ⇒ Some(m.command → m.text)) should contain theSameElementsAs commands

      val messages4000 = hook4000.getMessages
      messages4000 should have size 4
      messages4000.map(m ⇒ Some(m.command → m.text)) should contain theSameElementsAs commands
    }
  }

  class DummyHookListener(port: Int)(implicit system: ActorSystem, materializer: Materializer) extends PlayJsonSupport {

    import akka.http.scaladsl.Http
    import akka.http.scaladsl.server.Directives._
    import akka.http.scaladsl.server.Route
    import im.actor.server.api.rpc.service.messaging.ReverseHooksWorker._
    import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._

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
