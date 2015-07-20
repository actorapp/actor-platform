package im.actor.server.http

import java.nio.file.Paths

import im.actor.server.group.GroupOffice
import im.actor.server.user.UserOffice

import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.{ OK, BadRequest, NotFound }
import akka.stream.scaladsl.Sink
import org.scalatest.Inside._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, StatusCodes }
import akka.util.ByteString
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import play.api.libs.json._

import im.actor.api.rpc.ClientData
import im.actor.server.api.http.json.{ JsonImplicits, AvatarUrls }
import im.actor.server.api.http.{ HttpApiConfig, HttpApiFrontend }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.{ GroupsServiceHelpers, messaging }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.social.SocialManager
import im.actor.server.util.{ ImageUtils, FileUtils, ACLUtils }
import im.actor.server.{ ImplicitFileStorageAdapter, BaseAppSuite, models, persist }

class HttpApiFrontendSpec extends BaseAppSuite with GroupsServiceHelpers with ImplicitFileStorageAdapter {
  behavior of "HttpApiFrontend"

  "Webhooks handler" should "respond with OK to webhooks text message" in t.textMessage()

  //  it should "respond with OK to webhooks document message" in t.documentMessage()//TODO: not implemented yet

  //  it should "respond with OK to webhooks image message" in t.imageMessage()//TODO: not implemented yet

  "Groups handler" should "respond with JSON message to group invite info with correct invite token" in t.groupInvitesOk()

  it should "respond with JSON message with avatar full links to group invite info with correct invite token" in t.groupInvitesAvatars1()

  it should "respond with JSON message with avatar partial links to group invite info with correct invite token" in t.groupInvitesAvatars2()

  it should "respond with Not Acceptable to group invite info with invalid invite token" in t.groupInvitesInvalid()

  it should "respond BadRequest to unknown message format" in t.malformedMessage()

  "Files handler" should "respond with not found to non existing file" in t.notFound()

  it should "not allow path traversal" in t.pathTraversal()

  it should "serve correct file path" in t.filesCorrect()

  implicit val sessionRegion = buildSessionRegionProxy()
  implicit val seqUpdManagerRegion = buildSeqUpdManagerRegion()
  implicit val socialManagerRegion = SocialManager.startRegion()
  implicit val presenceManagerRegion = PresenceManager.startRegion()
  implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegion()
  implicit val privatePeerManagerRegion = UserOffice.startRegion()
  implicit val groupPeerManagerRegion = GroupOffice.startRegion()

  val awsCredentials = new EnvironmentVariableCredentialsProvider()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit val service = messaging.MessagingServiceImpl(mediator)
  val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  implicit val oauth2Service = new GoogleProvider(oauthGoogleConfig)
  implicit val authService = buildAuthService()
  implicit val groupsService = new GroupsServiceImpl(groupInviteConfig)

  val s3BucketName = fsAdapter.bucketName

  object t {
    val (user1, authId1, _) = createUser()
    val (user2, authId2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(user1.id))

    val groupName = "Test group"
    val groupOutPeer = createGroup(groupName, Set(user2.id)).groupPeer

    val resourcesPath = Paths.get(getClass.getResource("/").toURI).toFile.getCanonicalPath
    val config = HttpApiConfig("127.0.0.1", 9000, "http", "localhost", resourcesPath, None)
    HttpApiFrontend.start(config, None)

    val http = Http()

    def textMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/webhooks/$botToken",
          entity = """{"text":"Good morning everyone!"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual OK
        }
      }
    }

    def documentMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/webhooks/$botToken",
          entity = """{"document_url":"http://www.scala-lang.org/docu/files/ScalaReference.pdf"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual OK
        }
      }
    }

    def imageMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/webhooks/$botToken",
          entity = """{"image_url":"http://www.scala-lang.org/resources/img/smooth-spiral.png"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual OK
        }
      }
    }

    def malformedMessage() = {
      whenReady(db.run(persist.GroupBot.findByGroup(groupOutPeer.groupId))) { bot ⇒
        bot shouldBe defined
        val botToken = bot.get.token
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/webhooks/$botToken",
          entity = """{"WRONG":"Should not be parsed"}"""
        )
        whenReady(http.singleRequest(request)) { resp ⇒
          resp.status shouldEqual BadRequest
        }
      }
    }

    def groupInvitesOk() = {
      val token = ACLUtils.accessToken(ThreadLocalRandom.current())
      val inviteToken = models.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteToken.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/groups/invites/$token"
        )
        val resp = whenReady(http.singleRequest(request))(identity)
        resp.status shouldEqual OK
        whenReady(resp.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.decodeString("utf-8"))) { body ⇒
          val response = Json.parse(body)
          (response \ "group" \ "title").as[String] shouldEqual groupName
          (response \ "inviter" \ "name").as[String] shouldEqual user1.name
        }
      }
    }

    def groupInvitesAvatars1() = {
      val avatarFile = Paths.get(getClass.getResource("/valid-avatar.jpg").toURI).toFile
      val fileLocation = whenReady(db.run(fsAdapter.uploadFile("avatar", avatarFile)))(identity)

      whenReady(db.run(ImageUtils.scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current()))) { result ⇒
        result should matchPattern { case Right(_) ⇒ }
        val avatar = ImageUtils.getAvatarData(models.AvatarData.OfGroup, groupOutPeer.groupId, result.right.toOption.get)
        whenReady(db.run(persist.AvatarData.createOrUpdate(avatar)))(_ ⇒ ())
      }

      val token = ACLUtils.accessToken(ThreadLocalRandom.current())
      val inviteToken = models.GroupInviteToken(groupOutPeer.groupId, user1.id, token)

      whenReady(db.run(persist.GroupInviteToken.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/groups/invites/$token"
        )

        val resp = whenReady(http.singleRequest(request))(identity)
        resp.status shouldEqual OK

        whenReady(resp.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.decodeString("utf-8"))) { body ⇒
          import JsonImplicits.avatarUrlsFormat

          val response = Json.parse(body)
          (response \ "group" \ "title").as[String] shouldEqual groupName
          (response \ "inviter" \ "name").as[String] shouldEqual user1.name

          val avatarUrls = (response \ "group" \ "avatars").as[AvatarUrls]
          inside(avatarUrls) {
            case AvatarUrls(Some(small), Some(large), Some(full)) ⇒
              List(small, large, full) foreach (_ should startWith(s"https://$s3BucketName.s3.amazonaws.com"))
          }
          (response \ "inviter" \ "avatars").as[AvatarUrls] should matchPattern {
            case AvatarUrls(None, None, None) ⇒
          }
        }
      }
    }

    def groupInvitesAvatars2() = {
      val avatarFile = Paths.get(getClass.getResource("/valid-avatar.jpg").toURI).toFile
      val fileLocation = whenReady(db.run(fsAdapter.uploadFile("avatar", avatarFile)))(identity)
      whenReady(db.run(ImageUtils.scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current()))) { result ⇒
        result should matchPattern { case Right(_) ⇒ }
        val avatar =
          ImageUtils.getAvatarData(models.AvatarData.OfGroup, groupOutPeer.groupId, result.right.toOption.get)
            .copy(smallAvatarFileId = None, smallAvatarFileHash = None, smallAvatarFileSize = None)
        whenReady(db.run(persist.AvatarData.createOrUpdate(avatar)))(_ ⇒ ())
      }

      val token = ACLUtils.accessToken(ThreadLocalRandom.current())
      val inviteToken = models.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteToken.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.scheme}://${config.host}:${config.port}/v1/groups/invites/$token"
        )
        val resp = whenReady(http.singleRequest(request))(identity)
        resp.status shouldEqual OK
        whenReady(resp.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.decodeString("utf-8"))) { body ⇒
          import JsonImplicits.avatarUrlsFormat

          val response = Json.parse(body)
          (response \ "group" \ "title").as[String] shouldEqual groupName
          (response \ "inviter" \ "name").as[String] shouldEqual user1.name
          val avatarUrls = (response \ "group" \ "avatars").as[AvatarUrls]
          inside(avatarUrls) {
            case AvatarUrls(None, Some(large), Some(full)) ⇒
              List(large, full) foreach (_ should startWith(s"https://$s3BucketName.s3.amazonaws.com"))
          }
          (response \ "inviter" \ "avatars").as[AvatarUrls] should matchPattern {
            case AvatarUrls(None, None, None) ⇒
          }
        }
      }
    }

    def groupInvitesInvalid() = {
      val invalidToken = "Dkajsdljasdlkjaskdj329u90u32jdjlksRandom_stuff"
      val request = HttpRequest(
        method = HttpMethods.GET,
        uri = s"${config.scheme}://${config.host}:${config.port}/v1/groups/invites/$invalidToken"
      )
      val resp = whenReady(http.singleRequest(request))(identity)
      resp.status shouldEqual StatusCodes.NotAcceptable
    }

    def notFound() = {
      val request = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/neverExisted.txt")
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual NotFound
        //todo: remove when this https://github.com/akka/akka/issues/17403 solved
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
    }

    def pathTraversal() = {
      val attack1 = "%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2Fpasswd"
      val r1 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack1")
      whenReady(http.singleRequest(r1)) { resp ⇒
        resp.status shouldEqual NotFound
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val attack2 = "..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fetc%2Fpasswd"
      val r2 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack2")
      whenReady(http.singleRequest(r2)) { resp ⇒
        resp.status shouldEqual NotFound
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val attack3 = "../../../../../../../../etc/passwd"
      val r3 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack3")
      whenReady(http.singleRequest(r3)) { resp ⇒
        resp.status shouldEqual NotFound
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
    }

    def filesCorrect() = {
      val r1 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/reference.conf")
      whenReady(http.singleRequest(r1)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val r2 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/logback.xml")
      whenReady(http.singleRequest(r2)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val r3 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/valid-avatar.jpg")
      whenReady(http.singleRequest(r3)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val r4 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/application.conf.example")
      whenReady(http.singleRequest(r4)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
    }
  }

}
