package im.actor.server.api.rpc.service

import java.nio.file.{ Files, Paths }

import im.actor.server.file.ImageUtils

import scala.concurrent.Await
import scala.concurrent.duration._
import scalaz.-\/

import com.sksamuel.scrimage.AsyncImage
import org.scalatest.Inside._

import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.server._
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.api.rpc.service.profile.{ ProfileErrors, ProfileServiceImpl }
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }

class ProfileServiceSpec
  extends BaseAppSuite
  with ImplicitFileStorageAdapter
  with ImplicitUserRegions
  with ImplicitSessionRegionProxy
  with ImplicitAuthService {
  behavior of "Profile Service"

  it should "Set user avatar" in profile.e1
  it should "Respond with LOCATION_INVALID on invalid image" in profile.e2
  it should "Respond with FILE_TOO_LARGE on too large image" in profile.e3

  "Nickname check and edit" should "work correct with different nicknames" in profile.e4

  "EditAbout" should "set valid about value to user" in profile.e5

  implicit lazy val service = new ProfileServiceImpl
  implicit lazy val filesService = new FilesServiceImpl

  private val invalidImageFile = Paths.get(getClass.getResource("/invalid-avatar.jpg").toURI).toFile
  private val tooLargeImageFile = Paths.get(getClass.getResource("/too-large-avatar.jpg").toURI).toFile

  private val validOrigBytes =
    Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
  private val validOrigFile = Paths.get(getClass.getResource("/valid-avatar.jpg").toURI).toFile
  private val validOrigAImg = Await.result(AsyncImage(validOrigFile), 5.seconds)

  private val validOrigDimensions = ImageUtils.dimensions(validOrigAImg)

  private val validSmallFile = Paths.get(getClass.getResource("/valid-avatar-small.jpg").toURI).toFile
  private val validSmallBytes = org.apache.commons.io.FileUtils.readFileToByteArray(validSmallFile)
  private val validSmallDimensions = (100, 100)

  private val validLargeFile = Paths.get(getClass.getResource("/valid-avatar-large.jpg").toURI).toFile
  private val validLargeBytes = org.apache.commons.io.FileUtils.readFileToByteArray(validLargeFile)
  private val validLargeDimensions = (200, 200)

  object profile {
    val (user, _, _) = createUser()

    val authId = createAuthId()
    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId, sessionId, Some(user.id))

    def e1() = {
      val validOrigFileModel = Await.result(db.run(fsAdapter.uploadFile("avatar.jpg", validOrigFile)), 5.seconds)

      whenReady(service.handleEditAvatar(ApiFileLocation(validOrigFileModel.fileId, validOrigFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case Ok(_) ⇒
        }

        val r = resp.toOption.get

        r.avatar.fullImage.get.width should ===(validOrigDimensions._1)
        r.avatar.fullImage.get.height should ===(validOrigDimensions._2)
        r.avatar.fullImage.get.fileSize should ===(validOrigBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.fullImage.get.fileLocation.fileId))) { fileOpt ⇒
          org.apache.commons.io.FileUtils.readFileToByteArray(fileOpt.get) should ===(validOrigBytes)
        }

        r.avatar.smallImage.get.width should ===(validSmallDimensions._1)
        r.avatar.smallImage.get.height should ===(validSmallDimensions._2)
        r.avatar.smallImage.get.fileSize should ===(validSmallBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.smallImage.get.fileLocation.fileId))) { fileOpt ⇒
          org.apache.commons.io.FileUtils.readFileToByteArray(fileOpt.get) should ===(validSmallBytes)
        }

        r.avatar.largeImage.get.width should ===(validLargeDimensions._1)
        r.avatar.largeImage.get.height should ===(validLargeDimensions._2)
        r.avatar.largeImage.get.fileSize should ===(validLargeBytes.length)
        whenReady(db.run(fsAdapter.downloadFile(r.avatar.largeImage.get.fileLocation.fileId))) { fileOpt ⇒
          org.apache.commons.io.FileUtils.readFileToByteArray(fileOpt.get) should ===(validLargeBytes)
        }

      }
    }

    def e2() = {
      val invalidImageFileModel = Await.result(db.run(fsAdapter.uploadFile("invalid-avatar.jpg", invalidImageFile)), 5.seconds)

      whenReady(service.handleEditAvatar(ApiFileLocation(invalidImageFileModel.fileId, invalidImageFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case -\/(RpcError(400, "LOCATION_INVALID", _, _, _)) ⇒
        }
      }
    }

    def e3() = {
      val tooLargeImageFileModel = Await.result(db.run(fsAdapter.uploadFile("too-large-avatar.jpg", tooLargeImageFile)), 30.seconds) //WTF???

      whenReady(service.handleEditAvatar(ApiFileLocation(tooLargeImageFileModel.fileId, tooLargeImageFileModel.accessHash))) { resp ⇒
        resp should matchPattern {
          case -\/(RpcError(400, "FILE_TOO_LARGE", _, _, _)) ⇒
        }
      }
    }

    def e4() = {
      val (user1, authId1, _) = createUser()
      val (user2, authId2, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(authId1, sessionId, Some(user1.id))
      val clientData2 = ClientData(authId2, sessionId, Some(user2.id))

      whenReady(service.jhandleCheckNickName("rockjam", clientData1)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(true))
      }

      whenReady(service.jhandleEditNickName(Some("rockjam"), clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(service.jhandleEditNickName(Some("rockjam"), clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileErrors.NicknameBusy
        }
      }

      whenReady(service.jhandleCheckNickName("rockjam", clientData2)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(false))
      }

      whenReady(service.jhandleCheckNickName("rock-jam", clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileErrors.NicknameInvalid
        }
      }

      whenReady(service.jhandleEditNickName(Some("rock-jam"), clientData2)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileErrors.NicknameInvalid
        }
      }

      whenReady(service.jhandleEditNickName(None, clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(service.jhandleCheckNickName("rockjam", clientData2)) { resp ⇒
        resp shouldEqual Ok(ResponseBool(true))
      }

      whenReady(service.jhandleEditNickName(Some("rockjam"), clientData2)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }
    }

    def e5() = {
      val (user1, authId1, _) = createUser()
      val sessionId = createSessionId()

      val clientData1 = ClientData(authId1, sessionId, Some(user1.id))

      val about = Some("is' me")
      whenReady(service.jhandleEditAbout(about, clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(db.run(persist.User.find(user1.id).headOption)) { optUser ⇒
        optUser shouldBe defined
        optUser.get.about shouldEqual about
      }

      val tooLong = 1 to 300 map (e ⇒ ".") mkString ("")
      whenReady(service.jhandleEditAbout(Some(tooLong), clientData1)) { resp ⇒
        inside(resp) {
          case Error(e) ⇒ e shouldEqual ProfileErrors.AboutTooLong
        }
      }

      whenReady(service.jhandleEditAbout(None, clientData1)) { resp ⇒
        resp should matchPattern {
          case Ok(_: ResponseSeq) ⇒
        }
      }

      whenReady(db.run(persist.User.find(user1.id).headOption)) { optUser ⇒
        optUser shouldBe defined
        optUser.get.about shouldEqual None
      }

    }

  }

}
