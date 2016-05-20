package im.actor.server.api.rpc.service

import java.nio.file.{ Files, Paths }

import cats.data.Xor
import im.actor.server.file.{ UnsafeFileName, FileStorageExtension, ImageUtils }

import scala.concurrent.Await
import scala.concurrent.duration._

import com.sksamuel.scrimage.Image

import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.server._
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.profile.{ ProfileRpcErrors, ProfileServiceImpl }

final class ProfileServiceSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "Profile Service"

  it should "Set user avatar" in profile.e1
  it should "Respond with LOCATION_INVALID on invalid image" in profile.e2
  it should "Respond with FILE_TOO_LARGE on too large image" in profile.e3

  "Nickname check and edit" should "work correct with different nicknames" in profile.e4

  "EditAbout" should "set valid about value to user" in profile.e5

  "ChangeMyTimeZone" should "change time zone" in profile.timeZone
  it should "respond with error on invalid time zone" in profile.invalidTimeZone
  it should "respond with error on same time zone" in profile.sameTimeZone

  "ChangeMyPreferredLanguages" should "change preferred languages" in profile.preferredLanguages
  it should "respond with error on invalid locale" in profile.invalidPreferredLanguages
  it should "respond with error on same preferred languages" in profile.samePreferredLanguages

  "Edit name" should "not allow to use empty name" in profile.editNameEmpty

  implicit lazy val service = new ProfileServiceImpl
  implicit lazy val filesService = new FilesServiceImpl

  private val fsAdapter = FileStorageExtension(system).fsAdapter

  private val invalidImageFile = Files.readAllBytes(Paths.get(getClass.getResource("/invalid-avatar.jpg").toURI))
  private val tooLargeImageFile = Files.readAllBytes(Paths.get(getClass.getResource("/too-large-avatar.jpg").toURI))

  private val validOrigBytes = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
  private val validOrigFile = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
  private val validOrigAImg = Image(validOrigFile).toPar

  private val validOrigDimensions = ImageUtils.dimensions(validOrigAImg)

  private val validSmallFile = Paths.get(getClass.getResource("/valid-avatar-small.jpg").toURI).toFile
  private val validSmallBytes = org.apache.commons.io.FileUtils.readFileToByteArray(validSmallFile)
  private val validSmallDimensions = (100, 100)

  private val validLargeFile = Paths.get(getClass.getResource("/valid-avatar-large.jpg").toURI).toFile
  private val validLargeBytes = org.apache.commons.io.FileUtils.readFileToByteArray(validLargeFile)
  private val validLargeDimensions = (200, 200)

  object profile {
    val (user, authId, authSid, _) = createUser()
    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    def e1() = {
      val validOrigFileModel = Await.result(db.run(fsAdapter.uploadFile(UnsafeFileName("/etc/passwd/avatar.jpg"), validOrigFile)), 5.seconds)

      whenReady(service.handleEditAvatar(ApiFileLocation(validOrigFileModel.fileId, validOrigFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case Ok(_) ⇒
        }

        val r = resp.toOption.get

        r.avatar.fullImage.get.width should ===(validOrigDimensions._1)
        r.avatar.fullImage.get.height should ===(validOrigDimensions._2)
        r.avatar.fullImage.get.fileSize should ===(validOrigBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.fullImage.get.fileLocation.fileId))) { fileOpt ⇒
          fileOpt.get should ===(validOrigBytes)
        }

        r.avatar.smallImage.get.width should ===(validSmallDimensions._1)
        r.avatar.smallImage.get.height should ===(validSmallDimensions._2)
        r.avatar.smallImage.get.fileSize should ===(validSmallBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.smallImage.get.fileLocation.fileId))) { fileOpt ⇒
          fileOpt.get should ===(validSmallBytes)
        }

        r.avatar.largeImage.get.width should ===(validLargeDimensions._1)
        r.avatar.largeImage.get.height should ===(validLargeDimensions._2)
        r.avatar.largeImage.get.fileSize should ===(validLargeBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.largeImage.get.fileLocation.fileId))) { fileOpt ⇒
          fileOpt.get should ===(validLargeBytes)
        }

      }
    }

    def e2() = {
      val invalidImageFileModel = Await.result(db.run(fsAdapter.uploadFile(UnsafeFileName("invalid-avatar.jpg"), invalidImageFile)), 5.seconds)

      whenReady(service.handleEditAvatar(ApiFileLocation(invalidImageFileModel.fileId, invalidImageFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case Xor.Left(RpcError(400, "LOCATION_INVALID", _, _, _)) ⇒
        }
      }
    }

    def e3() = {
      val tooLargeImageFileModel = Await.result(db.run(fsAdapter.uploadFile(UnsafeFileName("too-large-avatar.jpg"), tooLargeImageFile)), 30.seconds) //WTF???

      whenReady(service.handleEditAvatar(ApiFileLocation(tooLargeImageFileModel.fileId, tooLargeImageFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case Xor.Left(RpcError(400, "FILE_TOO_LARGE", _, _, _)) ⇒
        }
      }
    }

    def e4() = {
      val (user1, authId1, authSid1, _) = createUser()
      val (user2, authId2, authSid2, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))
      val clientData2 = ClientData(authId2, sessionId, Some(AuthData(user2.id, authSid2, 42)))

      whenReady(service.handleCheckNickName("rockjam")(clientData1)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(true))
      }

      whenReady(service.handleEditNickName(Some("rockjam"))(clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(service.handleEditNickName(Some("rockjam"))(clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileRpcErrors.NicknameBusy
        }
      }

      whenReady(service.handleCheckNickName("rockjam")(clientData2)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(false))
      }

      whenReady(service.handleCheckNickName("rock-jam")(clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileRpcErrors.NicknameInvalid
        }
      }

      whenReady(service.handleEditNickName(Some("rock-jam"))(clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileRpcErrors.NicknameInvalid
        }
      }

      whenReady(service.handleEditNickName(None)(clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(service.handleCheckNickName("rockjam")(clientData2)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(true))
      }

      whenReady(service.handleEditNickName(Some("rockjam"))(clientData2)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
    }

    def e5() = {
      val (user1, authId1, authSid1, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(authId1, sessionId, Some(AuthData(user1.id, authSid1, 42)))

      val about = Some("is' me")
      whenReady(service.handleEditAbout(about)(clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(db.run(persist.UserRepo.find(user1.id))) { optUser ⇒
        optUser shouldBe defined
        optUser.get.about shouldEqual about
      }

      val tooLong = 1 to 300 map (e ⇒ ".") mkString ""
      whenReady(service.handleEditAbout(Some(tooLong))(clientData1)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileRpcErrors.AboutTooLong
        }
      }

      whenReady(service.handleEditAbout(None)(clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(db.run(persist.UserRepo.find(user1.id))) { optUser ⇒
        optUser shouldBe defined
        optUser.get.about shouldEqual None
      }

    }

    def timeZone() = {
      val (user, authId, authSid, _) = createUser()

      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      whenReady(service.handleEditMyTimeZone("Africa/Addis_Ababa")) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
    }

    def invalidTimeZone() = {
      val (user, authId, authSid, _) = createUser()

      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      whenReady(service.handleEditMyTimeZone("Africa/Addis_AbEba")) { resp ⇒
        inside(resp) {
          case Error(RpcError(400, "INVALID_TIME_ZONE", _, false, _)) ⇒
        }
      }
    }

    def sameTimeZone() = {
      val (user, authId, authSid, _) = createUser()

      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      val tz = "Africa/Addis_Ababa"

      whenReady(service.handleEditMyTimeZone(tz)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
      whenReady(service.handleEditMyTimeZone(tz)) { resp ⇒
        inside(resp) {
          case Error(RpcError(400, "UPDATE_ALREADY_APPLIED", _, false, _)) ⇒
        }
      }
    }

    def preferredLanguages() = {
      val (user, authId, authSid, _) = createUser()

      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      whenReady(service.handleEditMyPreferredLanguages(Vector("pt-BR", "en-US", "ru"))) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
    }

    def invalidPreferredLanguages() = {
      val (user, authId, authSid, _) = createUser()

      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      whenReady(service.handleEditMyPreferredLanguages(Vector("pt-br"))) { resp ⇒
        inside(resp) {
          case Error(RpcError(400, "INVALID_LOCALE", _, false, _)) ⇒
        }
      }

      whenReady(service.handleEditMyPreferredLanguages(Vector.empty)) { resp ⇒
        inside(resp) {
          case Error(RpcError(400, "EMPTY_LOCALES_LIST", _, false, _)) ⇒
        }
      }
    }

    def samePreferredLanguages() = {
      implicit val clientData = ClientData(authId, 1, Some(AuthData(user.id, authSid, 42)))
      val langs = Vector("pt-BR", "en-US", "ru")

      whenReady(service.handleEditMyPreferredLanguages(langs)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
      whenReady(service.handleEditMyPreferredLanguages(langs)) { resp ⇒
        inside(resp) {
          case Error(RpcError(400, "UPDATE_ALREADY_APPLIED", _, false, _)) ⇒
        }
      }
    }

    def editNameEmpty() = {
      val (user, authId, authSid, _) = createUser()
      val sessionId = createSessionId()

      whenReady(service.handleEditName("")(clientData)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileRpcErrors.NameInvalid
        }
      }

    }
  }
}
