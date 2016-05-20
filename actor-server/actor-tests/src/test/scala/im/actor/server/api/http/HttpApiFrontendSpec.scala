package im.actor.server.api.http

import java.nio.file.{ Files, Paths }

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{ DELETE, GET, POST }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.http.scaladsl.unmarshalling._
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.http.json.JsonFormatters._
import im.actor.server.api.http.json.{ AvatarUrls, _ }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.file.{ UnsafeFileName, FileStorageExtension, ImageUtils }
import im.actor.server.webhooks.WebhooksExtension
import im.actor.server.webhooks.http.routes.OutgoingHooksErrors
import im.actor.util.ThreadLocalSecureRandom
import play.api.libs.json._

final class HttpApiFrontendSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with ImplicitSessionRegion
  with ImplicitAuthService
  with PlayJsonSupport {
  behavior of "HttpApiFrontend"

  "Webhooks handler" should "respond with OK to webhooks text message" in t.textMessage()

  //  it should "respond with Forbidden in public groups" in t.publicGroups()

  it should "respond with BadRequest to non existing groups" in t.nonExistingBot()

  //  it should "respond with OK to webhooks document message" in t.documentMessage()//TODO: not implemented yet

  //  it should "respond with OK to webhooks image message" in t.imageMessage()//TODO: not implemented yet

  "Reverse hooks handler" should "not register outgoing hook with wrong url" in t.registerWrongUrl()

  it should "not register outgoing hook with wrong token" in t.registerWrongToken()

  it should "register outgoing hook with correct url" in t.registerCorrectUrl()

  it should "not register duplicated hooks" in t.registerDuplicated()

  it should "register many hooks for one group" in t.registerManyHooks()

  it should "list registered hooks" in t.listHooks()

  it should "register hook with id, get hook's status by id and remove hook by id" in t.hookStatus()

  "Groups handler" should "respond with JSON message to group invite info with correct invite token" in t.groupInvitesOk()

  it should "respond with JSON message with avatar full links to group invite info with correct invite token" in t.groupInvitesAvatars1()

  it should "respond with JSON message with avatar partial links to group invite info with correct invite token" in t.groupInvitesAvatars2()

  it should "respond with Not Acceptable to group invite info with invalid invite token" in t.groupInvitesInvalid()

  it should "respond BadRequest to unknown message format" in t.malformedMessage()

  "Files handler" should "respond with not found to non existing file" in t.notFound()

  it should "not allow path traversal" in t.pathTraversal()

  it should "serve correct file path" in t.filesCorrect()

  val groupInviteConfig = GroupInviteConfig("http://actor.im")

  implicit lazy val service = messaging.MessagingServiceImpl()
  implicit lazy val groupsService = new GroupsServiceImpl(groupInviteConfig)

  private val fsAdapter = FileStorageExtension(system).fsAdapter

  WebhooksExtension(system) //initialize webhooks routes

  implicit val reverseHookResponseUnmarshaller: FromEntityUnmarshaller[ReverseHookResponse] = Unmarshaller { implicit ec ⇒ entity ⇒
    Unmarshal(entity).to[String].map { body ⇒
      Json.parse(body).as[ReverseHookResponse]
    }
  }

  implicit val statusUnmarshaller: FromEntityUnmarshaller[Status] = Unmarshaller { implicit ec ⇒ entity ⇒
    Unmarshal(entity).to[String].map { body ⇒
      Json.parse(body).as[Status]
    }
  }

  val s3BucketName = "actor-uploads-test"

  object t {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val groupName = "Test group"
    val groupOutPeer = createGroup(groupName, Set(user2.id)).groupPeer
    val publicGroup = createPubGroup("public group", "PG", Set(user2.id)).groupPeer

    val resourcesPath = Paths.get(getClass.getResource("/files").toURI).toFile.getCanonicalPath
    val config = HttpApiConfig("127.0.0.1", 9090, "http://localhost:9090", resourcesPath, None)
    HttpApiFrontend.start(config)

    val http = Http()

    def textMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"text":"Good morning everyone!"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
      }
    }

    def publicGroups() = {
      val token = extractToken(publicGroup.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"text":"FLOOD FLOOD FLOOD"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual StatusCodes.Forbidden
      }
    }

    def nonExistingBot() = {
      val wrongToken = "xxx"
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$wrongToken",
        entity = """{"text":"Bla bla bla"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual StatusCodes.BadRequest
      }

    }

    def documentMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"document_url":"http://www.scala-lang.org/docu/files/ScalaReference.pdf"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
      }
    }

    def imageMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"image_url":"http://www.scala-lang.org/resources/img/smooth-spiral.png"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
      }
    }

    def registerWrongUrl() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token/reverse",
        entity = """{"url":"This is wrong url"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual BadRequest
        whenReady(Unmarshal(resp.entity).to[Errors]) { errors ⇒
          errors.message shouldEqual OutgoingHooksErrors.MalformedUri
        }
      }
    }

    def registerWrongToken() = {
      val wrongToken = "xxx"
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$wrongToken/reverse",
        entity = """{"url":"http://zapier.com/11"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual NotFound
        whenReady(Unmarshal(resp.entity).to[Errors]) { errors ⇒
          errors.message shouldEqual OutgoingHooksErrors.WrongIntegrationToken
        }
      }
    }

    def registerCorrectUrl() = {
      val token = extractToken(groupOutPeer.groupId)
      val hookUrl = "https://zapier.com/0"
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token/reverse",
        entity = s"""{"target_url":"$hookUrl", "other_url":"http://foo.bar"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual Created
        whenReady(Unmarshal(resp.entity).to[ReverseHookResponse]) { response ⇒
          response.id > 0 shouldBe true
          response.url shouldEqual None
        }
      }
    }

    def registerDuplicated() = {
      val token = extractToken(groupOutPeer.groupId)
      val duplicatedUrl = "https://zapier.com/0"
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token/reverse",
        entity = s"""{"url":"$duplicatedUrl"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual Conflict
        whenReady(Unmarshal(resp.entity).to[Errors]) { errors ⇒
          errors.message shouldEqual OutgoingHooksErrors.AlreadyRegistered
        }
      }
    }

    def registerManyHooks() = {
      val token = extractToken(groupOutPeer.groupId)
      val httpApiUrl = s"${config.baseUri}/v1/webhooks/$token/reverse"

      for (i ← 1 to 5) {
        whenReady(http.singleRequest(HttpRequest(POST, httpApiUrl, entity = s"""{"url":"https://zapier.com/$i"}"""))) { resp ⇒
          resp.status shouldEqual Created
          resp.entity.dataBytes.runWith(Sink.ignore)
        }
      }
    }

    def listHooks() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = GET,
        uri = s"${config.baseUri}/v1/webhooks/$token/reverse"
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
        whenReady(Unmarshal(resp.entity).to[List[ReverseHookResponse]]) { hooks ⇒
          val expected = 0 to 5 map (i ⇒ s"https://zapier.com/$i")
          hooks.map(_.url).flatten should contain theSameElementsAs expected
          hooks.map(_.id) foreach (_ > 0 shouldBe true)
        }
      }
    }

    def hookStatus() = {
      val token = extractToken(groupOutPeer.groupId)
      val hookUrl = "https://zapier.com/77"
      val baseUri = s"${config.baseUri}/v1/webhooks/$token/reverse"
      val request = HttpRequest(POST, baseUri, entity = s"""{"url":"$hookUrl"}""")

      val hookId = whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual Created
        whenReady(Unmarshal(resp.entity).to[ReverseHookResponse])(_.id)
      }

      val hookUri = s"$baseUri/$hookId"
      whenReady(http.singleRequest(HttpRequest(GET, hookUri))) { resp ⇒
        resp.status shouldEqual OK
        whenReady(Unmarshal(resp.entity).to[Status]) { status ⇒
          status.status shouldEqual "Ok"
        }
      }

      whenReady(http.singleRequest(HttpRequest(DELETE, hookUri))) { resp ⇒
        resp.status shouldEqual StatusCodes.Accepted
        whenReady(Unmarshal(resp.entity).to[Status]) { status ⇒
          status.status shouldEqual "Ok"
        }
      }

      whenReady(http.singleRequest(HttpRequest(GET, hookUri))) { resp ⇒
        resp.status shouldEqual Gone
        whenReady(Unmarshal(resp.entity).to[Status]) { status ⇒
          status.status shouldEqual OutgoingHooksErrors.WebhookGone
        }
      }
    }

    def malformedMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"WRONG":"Should not be parsed"}"""
      )
      whenReady(http.singleRequest(request)) { resp ⇒
        resp.status shouldEqual BadRequest
      }
    }

    def groupInvitesOk() = {
      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
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
      val avatarData = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
      val fileLocation = whenReady(db.run(fsAdapter.uploadFile(UnsafeFileName("avatar"), avatarData)))(identity)

      whenReady(db.run(ImageUtils.scaleAvatar(fileLocation.fileId, ThreadLocalSecureRandom.current()))) { result ⇒
        result should matchPattern { case Right(_) ⇒ }
        val avatar = ImageUtils.getAvatarData(im.actor.server.model.AvatarData.OfGroup, groupOutPeer.groupId, result.right.toOption.get)
        whenReady(db.run(persist.AvatarDataRepo.createOrUpdate(avatar)))(_ ⇒ ())
      }

      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)

      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
        )

        val resp = whenReady(http.singleRequest(request))(identity)
        resp.status shouldEqual OK

        whenReady(resp.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.decodeString("utf-8"))) { body ⇒
          import JsonFormatters.avatarUrlsFormat

          val response = Json.parse(body)
          (response \ "group" \ "title").as[String] shouldEqual groupName
          (response \ "inviter" \ "name").as[String] shouldEqual user1.name

          val avatarUrls = (response \ "group" \ "avatars").as[AvatarUrls]
          inside(avatarUrls) {
            case AvatarUrls(Some(small), Some(large), Some(full)) ⇒
              List(small, large, full) foreach (_ should startWith("http://"))
          }
          (response \ "inviter" \ "avatars").as[AvatarUrls] should matchPattern {
            case AvatarUrls(None, None, None) ⇒
          }
        }
      }
    }

    def groupInvitesAvatars2() = {
      val avatarData = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
      val fileLocation = whenReady(db.run(fsAdapter.uploadFile(UnsafeFileName("avatar"), avatarData)))(identity)
      whenReady(db.run(ImageUtils.scaleAvatar(fileLocation.fileId, ThreadLocalSecureRandom.current()))) { result ⇒
        result should matchPattern { case Right(_) ⇒ }
        val avatar =
          ImageUtils.getAvatarData(im.actor.server.model.AvatarData.OfGroup, groupOutPeer.groupId, result.right.toOption.get)
            .copy(smallAvatarFileId = None, smallAvatarFileHash = None, smallAvatarFileSize = None)
        whenReady(db.run(persist.AvatarDataRepo.createOrUpdate(avatar)))(_ ⇒ ())
      }

      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = HttpMethods.GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
        )
        val resp = whenReady(http.singleRequest(request))(identity)
        resp.status shouldEqual OK
        whenReady(resp.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.decodeString("utf-8"))) { body ⇒
          import JsonFormatters.avatarUrlsFormat

          val response = Json.parse(body)
          (response \ "group" \ "title").as[String] shouldEqual groupName
          (response \ "inviter" \ "name").as[String] shouldEqual user1.name
          val avatarUrls = (response \ "group" \ "avatars").as[AvatarUrls]
          inside(avatarUrls) {
            case AvatarUrls(None, Some(large), Some(full)) ⇒
              List(large, full) foreach (_ should startWith("http://"))
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
        uri = s"${config.baseUri}/v1/groups/invites/$invalidToken"
      )
      val resp = whenReady(http.singleRequest(request))(identity)
      resp.status shouldEqual NotAcceptable
      resp.entity.dataBytes.runWith(Sink.ignore)
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
      val r1 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/index.html", entity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`))
      whenReady(http.singleRequest(r1)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val r2 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/test.conf", entity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`))
      whenReady(http.singleRequest(r2)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
      val r3 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/scripts/test.js", entity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`))
      whenReady(http.singleRequest(r3)) { resp ⇒
        resp.status shouldEqual OK
        resp.entity.dataBytes.runWith(Sink.ignore)
      }
    }
  }

}
