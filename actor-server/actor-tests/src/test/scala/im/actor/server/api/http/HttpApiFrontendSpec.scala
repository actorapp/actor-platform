package im.actor.server.api.http

import java.nio.file.{ Files, Paths }

import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server._
import im.actor.server.acl.ACLUtils
import im.actor.server.api.http.json.JsonFormatters._
import im.actor.server.api.http.json.{ AvatarUrls, _ }
import im.actor.server.api.rpc.service.groups.{ GroupInviteConfig, GroupsServiceImpl }
import im.actor.server.api.rpc.service.messaging
import im.actor.server.file.{ FileStorageExtension, ImageUtils, UnsafeFileName }
import im.actor.server.group.GroupExtension
import im.actor.server.webhooks.WebhooksExtension
import im.actor.server.webhooks.http.routes.OutgoingHooksErrors
import im.actor.util.ThreadLocalSecureRandom
import play.api.libs.json._
import spray.client.pipelining._
import spray.http.HttpMethods.{ DELETE, GET, POST }
import spray.http.StatusCodes._
import spray.http.{ HttpRequest, StatusCodes }
import spray.httpx.PlayJsonSupport
import spray.httpx.unmarshalling._

final class HttpApiFrontendSpec
  extends BaseAppSuite
  with GroupsServiceHelpers
  with ImplicitSessionRegion
  with ImplicitAuthService
  with PlayJsonSupport {
  behavior of "HttpApiFrontend"

  "Webhooks handler" should "respond with OK to webhooks text message" in t.textMessage()

  it should "respond with Forbidden in public groups" in pendingUntilFixed(t.publicGroups())

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

  it should "respond with JSON message without inviter, when we join via group short name" in t.groupInvitesShortName()

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
  val groupExt = GroupExtension(system)

  WebhooksExtension(system) //initialize webhooks routes

  val singleRequest = sendReceive

  object t {
    val (user1, authId1, authSid1, _) = createUser()
    val (user2, authId2, authSid2, _) = createUser()
    val sessionId = createSessionId()
    implicit val clientData = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

    val groupName = "Test group"
    val groupOutPeer = createGroup(groupName, Set(user2.id)).groupPeer
    //    val publicGroup = createPubGroup("public group", "PG", Set(user2.id)).groupPeer

    val resourcesPath = Paths.get(getClass.getResource("/files").toURI).toFile.getCanonicalPath
    val config = {
      val port = NetworkHelpers.randomPort()
      HttpApiConfig("127.0.0.1", port, s"http://localhost:$port", resourcesPath, None)
    }
    HttpApi(system).start(config)

    def textMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"text":"Good morning everyone!"}"""
      )
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
      }
    }

    def publicGroups() = {
      fail("Public groups are not implemented")
      //      val token = extractToken(publicGroup.groupId)
      //      val request = HttpRequest(
      //        method = POST,
      //        uri = s"${config.baseUri}/v1/webhooks/$token",
      //        entity = """{"text":"FLOOD FLOOD FLOOD"}"""
      //      )
      //      whenReady(singleRequest(request)) { resp ⇒
      //        resp.status shouldEqual StatusCodes.Forbidden
      //      }
    }

    def nonExistingBot() = {
      val wrongToken = "xxx"
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$wrongToken",
        entity = """{"text":"Bla bla bla"}"""
      )
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual BadRequest
      }

    }

    def documentMessage() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = POST,
        uri = s"${config.baseUri}/v1/webhooks/$token",
        entity = """{"document_url":"http://www.scala-lang.org/docu/files/ScalaReference.pdf"}"""
      )
      whenReady(singleRequest(request)) { resp ⇒
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
      whenReady(singleRequest(request)) { resp ⇒
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
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual BadRequest
        inside(resp.as[Errors]) {
          case Right(errors) ⇒ errors.message shouldEqual OutgoingHooksErrors.MalformedUri
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
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual NotFound
        inside(resp.as[Errors]) {
          case Right(errors) ⇒ errors.message shouldEqual OutgoingHooksErrors.WrongIntegrationToken
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
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual Created
        inside(resp.as[ReverseHookResponse]) {
          case Right(response) ⇒
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
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual Conflict
        inside(resp.as[Errors]) {
          case Right(errors) ⇒ errors.message shouldEqual OutgoingHooksErrors.AlreadyRegistered
        }
      }
    }

    def registerManyHooks() = {
      val token = extractToken(groupOutPeer.groupId)
      val httpApiUrl = s"${config.baseUri}/v1/webhooks/$token/reverse"

      for (i ← 1 to 5) {
        whenReady(singleRequest(HttpRequest(POST, httpApiUrl, entity = s"""{"url":"https://zapier.com/$i"}"""))) { resp ⇒
          resp.status shouldEqual Created
        }
      }
    }

    def listHooks() = {
      val token = extractToken(groupOutPeer.groupId)
      val request = HttpRequest(
        method = GET,
        uri = s"${config.baseUri}/v1/webhooks/$token/reverse"
      )
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual OK
        inside(resp.as[List[ReverseHookResponse]]) {
          case Right(hooks) ⇒
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

      val hookId = whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual Created
        resp.as[ReverseHookResponse].right.toOption.get.id
      }

      val hookUri = s"$baseUri/$hookId"
      whenReady(singleRequest(HttpRequest(GET, hookUri))) { resp ⇒
        resp.status shouldEqual OK
        inside(resp.as[Status]) {
          case Right(status) ⇒ status.status shouldEqual "Ok"
        }
      }

      whenReady(singleRequest(HttpRequest(DELETE, hookUri))) { resp ⇒
        resp.status shouldEqual StatusCodes.Accepted
        inside(resp.as[Status]) {
          case Right(status) ⇒ status.status shouldEqual "Ok"
        }
      }

      whenReady(singleRequest(HttpRequest(GET, hookUri))) { resp ⇒
        resp.status shouldEqual Gone
        inside(resp.as[Status]) {
          case Right(status) ⇒ status.status shouldEqual OutgoingHooksErrors.WebhookGone
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
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual BadRequest
      }
    }

    def groupInvitesOk() = {
      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
        )
        val resp = whenReady(singleRequest(request))(identity)
        resp.status shouldEqual OK
        val body = resp.entity.asString
        val response = Json.parse(body)
        (response \ "group" \ "title").as[String] shouldEqual groupName
        (response \ "inviter" \ "name").as[String] shouldEqual user1.name
      }
    }

    def groupInvitesShortName() = {
      val shortName = "division"
      whenReady(groupExt.updateShortName(groupOutPeer.groupId, user1.id, authId1, Some(shortName))) { _ ⇒
        val request = HttpRequest(
          method = GET,
          uri = s"${config.baseUri}/v1/groups/invites/$shortName"
        )
        val resp = singleRequest(request).futureValue
        resp.status shouldEqual OK
        val body = resp.entity.asString
        val response = Json.parse(body)
        (response \ "group" \ "title").as[String] shouldEqual groupName
        (response \ "inviter" \ "name").toOption shouldEqual None
      }
    }

    def groupInvitesAvatars1() = {
      val avatarData = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
      val fileLocation = db.run(fsAdapter.uploadFile(UnsafeFileName("avatar"), avatarData)).futureValue

      val avatar = {
        val scaleResult = db.run(ImageUtils.scaleAvatar(fileLocation.fileId)).futureValue
        scaleResult should matchPattern { case Right(_) ⇒ }
        scaleResult.right.toOption.get
      }
      whenReady(groupExt.updateAvatar(groupOutPeer.groupId, user1.id, authId1, Some(avatar), 432L))(identity)

      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)

      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
        )

        val resp = whenReady(singleRequest(request))(identity)
        resp.status shouldEqual OK

        val body = resp.entity.asString

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

    def groupInvitesAvatars2() = {
      val avatarData = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
      val fileLocation = db.run(fsAdapter.uploadFile(UnsafeFileName("avatar"), avatarData)).futureValue

      val avatar = {
        val scaleResult = db.run(ImageUtils.scaleAvatar(fileLocation.fileId)).futureValue
        scaleResult should matchPattern { case Right(_) ⇒ }
        scaleResult.right.toOption.get
      }
      whenReady(groupExt.updateAvatar(groupOutPeer.groupId, user1.id, authId1, Some(avatar), 433L))(identity)

      val token = ACLUtils.accessToken(ThreadLocalSecureRandom.current())
      val inviteToken = im.actor.server.model.GroupInviteToken(groupOutPeer.groupId, user1.id, token)
      whenReady(db.run(persist.GroupInviteTokenRepo.create(inviteToken))) { _ ⇒
        val request = HttpRequest(
          method = GET,
          uri = s"${config.baseUri}/v1/groups/invites/$token"
        )
        val resp = whenReady(singleRequest(request))(identity)
        resp.status shouldEqual OK
        val body = resp.entity.asString
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

    def groupInvitesInvalid() = {
      val invalidToken = "Dkajsdljasdlkjaskdj329u90u32jdjlksRandom_stuff"
      val request = HttpRequest(
        method = GET,
        uri = s"${config.baseUri}/v1/groups/invites/$invalidToken"
      )
      val resp = whenReady(singleRequest(request))(identity)
      resp.status shouldEqual NotAcceptable
    }

    def notFound() = {
      val request = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/neverExisted.txt")
      whenReady(singleRequest(request)) { resp ⇒
        resp.status shouldEqual NotFound
      }
    }

    def pathTraversal() = {
      val attack1 = "%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2Fpasswd"
      val r1 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack1")
      whenReady(singleRequest(r1)) { resp ⇒
        resp.status shouldEqual NotFound
      }
      val attack2 = "..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2Fetc%2Fpasswd"
      val r2 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack2")
      whenReady(singleRequest(r2)) { resp ⇒
        resp.status shouldEqual NotFound
      }
      val attack3 = "../../../../../../../../etc/passwd"
      val r3 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/$attack3")
      whenReady(singleRequest(r3)) { resp ⇒
        resp.status shouldEqual NotFound
      }
    }

    def filesCorrect() = {
      val r1 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/index.html")
      whenReady(singleRequest(r1)) { resp ⇒
        resp.status shouldEqual OK
      }
      val r2 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/test.conf")
      whenReady(singleRequest(r2)) { resp ⇒
        resp.status shouldEqual OK
      }
      val r3 = HttpRequest(GET, s"http://${config.interface}:${config.port}/app/scripts/test.js")
      whenReady(singleRequest(r3)) { resp ⇒
        resp.status shouldEqual OK
      }
    }
  }

}
