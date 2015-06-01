package im.actor.server.webhooks

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, StatusCodes }
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager

import im.actor.api.rpc.ClientData
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.{ GroupsServiceHelpers, messaging }
import im.actor.server.peermanagers.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.{ BaseAppSuite, persist }
import im.actor.server.api.rpc.service.messaging.{ GroupPeerManager, PrivatePeerManager }
import im.actor.server.api.rpc.service.{ GroupsServiceHelpers, messaging }
import im.actor.server.{ BaseAppSuite, persist }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager

class WebhooksFrontendSpec extends BaseAppSuite with GroupsServiceHelpers {
  behavior of "Webhooks frontend"

  it should "respond with OK to text message" in t.textMessage()

  //  it should "respond with OK to document message" in t.documentMessage()//TODO: not implemented yet

  //  it should "respond with OK to image message" in t.imageMessage()//TODO: not implemented yet

  it should "respond BadRequest" in t.malformedMessage()

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val privatePeerManagerRegion = PrivatePeerManager.startRegion()
  implicit val groupPeerManagerRegion = GroupPeerManager.startRegion()

  val bucketName = "actor-uploads-test"
  val awsCredentials = new EnvironmentVariableCredentialsProvider()
  implicit val transferManager = new TransferManager(awsCredentials)
  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val service = messaging.MessagingServiceImpl(mediator)
  implicit val authService = buildAuthService()
  implicit val groupsService = new GroupsServiceImpl("", groupInviteConfig)

  implicit val ec = system.dispatcher

  object t {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val groupOutPeer = createGroup("Bot test group", Set(user2.id)).groupPeer

    val config = WebhooksConfig("http", "localhost", 9000, "/v1/webhooks")
    WebhooksFrontend.start(config)

    val http = Http()

    def textMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"http://${config.interface}:${config.port}/v1/webhooks/$botToken",
          entity = """{"text":"Good morning everyone!"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual StatusCodes.OK
        }
      }
    }

    def documentMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"http://${config.interface}:${config.port}/v1/webhooks/$botToken",
          entity = """{"document_url":"http://www.scala-lang.org/docu/files/ScalaReference.pdf"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual StatusCodes.OK
        }
      }
    }

    def imageMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"http://${config.interface}:${config.port}/v1/webhooks/$botToken",
          entity = """{"image_url":"http://www.scala-lang.org/resources/img/smooth-spiral.png"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual StatusCodes.OK
        }
      }
    }

    def malformedMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"http://${config.interface}:${config.port}/v1/webhooks/$botToken",
          entity = """{"WRONG":"Should not be parsed"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual StatusCodes.BadRequest
        }
      }
    }
  }

}