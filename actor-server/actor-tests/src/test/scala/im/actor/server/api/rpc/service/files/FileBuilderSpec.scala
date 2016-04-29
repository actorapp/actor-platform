package im.actor.server.api.rpc.service.files

import java.nio.file.{ Files, Paths }
import java.util

import cats.data.Xor
import im.actor.api.rpc.{ AuthData, ClientData }
import im.actor.server.api.http.HttpApi
import im.actor.server.file.{ FileStorageExtension, UnsafeFileName }
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import spray.client.pipelining._
import spray.http.HttpHeaders.Location
import spray.http.HttpMethods.GET
import spray.http.{ HttpRequest, StatusCodes }

final class FileBuilderSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {

  behavior of "File builder"

  it should "generate correct redirect uri" in redirectUri

  private val fsAdapter = FileStorageExtension(system).fsAdapter
  private lazy val service = new FilesServiceImpl
  HttpApi(system).start()

  def redirectUri() = {
    val (user, authId, authSid, _) = createUser()
    val sessionId = createSessionId()

    implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

    val avatarData = Files.readAllBytes(Paths.get(getClass.getResource("/valid-avatar.jpg").toURI))
    val fileLocation = whenReady(db.run(fsAdapter.uploadFile(UnsafeFileName("avatar"), avatarData)))(identity)

    println(s"===File id: ${fileLocation.fileId}")
    println(s"===File access hash: ${fileLocation.accessHash}")

    val builder = whenReady(service.handleGetFileUrlBuilder(Vector("HMAC_SHA256"))) { resp ⇒
      resp should matchPattern {
        case Xor.Right(_) ⇒
      }
      resp.toOption.get
    }

    println(s"===Builder base url: ${builder.baseUrl}")
    println(s"===Builder secret: ${util.Arrays.toString(builder.signatureSecret)}")
    println(s"===Builder algo: ${builder.algo}")
    println(s"===Builder seed: ${builder.seed}")
    println(s"===Builder timeout: ${builder.timeout}")

    //    val seedBytes = Hex.decodeHex(builder.seed.toCharArray)
    //    val fileIdBytes = CalcSignature.getBytes(fileLocation.fileId)
    //    val accessHashBytes = CalcSignature.getBytes(fileLocation.accessHash)
    //
    //    println(s"===seed bytes: ${util.Arrays.toString(seedBytes)}")
    //    println(s"===file id bytes: ${util.Arrays.toString(fileIdBytes)}")
    //    println(s"===access hash bytes: ${util.Arrays.toString(accessHashBytes)}")
    //
    //    val toSignBytes = seedBytes ++ fileIdBytes ++ accessHashBytes
    //    println(s"===bytes to sign: ${util.Arrays.toString(toSignBytes)}")
    //
    //    val signPart = HmacUtils.hmacSha256Hex(builder.signatureSecret, toSignBytes)
    //
    //    val signature = builder.seed + "_" + signPart
    //    println(s"=== signature: ${signature}")

    val url = CalcSignature.fileBuilderUrl(
      builder.baseUrl,
      builder.seed,
      builder.signatureSecret,
      fileLocation.fileId,
      fileLocation.accessHash
    )

    println(s"=== file builder uri is: ${url}")

    val makeRequest = sendReceive

    val location = whenReady(makeRequest(HttpRequest(GET, url))) { resp ⇒
      resp.status shouldEqual StatusCodes.Found
      val optLocation = resp.header[Location]
      optLocation shouldBe defined
      optLocation.get.uri
    }

    println(s"=== location uri is: ${location}")

    whenReady(makeRequest(HttpRequest(GET, location))) { resp ⇒
      resp.entity.data.toByteArray shouldEqual avatarData
    }
  }

}
