package im.actor.server.api.rpc.service

import java.io.OutputStreamWriter
import java.net.{ HttpURLConnection, URL }

import im.actor.api.rpc.files._
import im.actor.api.rpc.{ AuthData, ClientData, Ok }
import im.actor.server.api.http.HttpApi
import im.actor.server.api.rpc.service.files.FilesServiceImpl
import im.actor.server.{ BaseAppSuite, ImplicitAuthService, ImplicitSessionRegion }
import org.apache.commons.io.IOUtils

final class FilesServiceSpec
  extends BaseAppSuite
  with ImplicitSessionRegion
  with ImplicitAuthService {
  behavior of "FilesService"

  it should "Generate upload url" in generateUploadUrl

  it should "Generate valid upload part urls" in generateUploadPartUrls

  it should "Complete upload" in completeUpload

  it should "Generate valid download urls" in generateValidDownloadUrls

  it should "Generate valid upload part urls when same request comes twice" in validUploadPartUrlsDuplRequest

  lazy val service = new FilesServiceImpl
  HttpApi(system).start()

  val (user, authId, authSid, _) = createUser()
  val sessionId = createSessionId()

  implicit val clientData = ClientData(authId, sessionId, Some(AuthData(user.id, authSid, 42)))

  var uploadKey: Array[Byte] = Array.empty

  var fileLocation: Option[ApiFileLocation] = None

  var expectedContents: Option[String] = None

  def generateUploadUrl() = {
    val size = 20

    whenReady(service.handleGetFileUploadUrl(size)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUploadUrl(url, key)) ⇒

      }

      this.uploadKey = resp.toOption.get.uploadKey
    }
  }

  def generateUploadPartUrls() = {
    val part1Size = 1024 * 32 // big part
    val part2Size = 5 // small part

    val resp1 = whenReady(service.handleGetFileUploadPartUrl(1, part1Size, uploadKey)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUploadPartUrl(_)) ⇒
      }

      (part1Size, resp)
    }

    val resp2 = whenReady(service.handleGetFileUploadPartUrl(2, part2Size, uploadKey)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUploadPartUrl(_)) ⇒
      }

      (part2Size, resp)
    }

    val parts = List(resp1, resp2) map {
      case (size, Ok(ResponseGetFileUploadPartUrl(urlStr))) ⇒
        val url = new URL(urlStr)
        val connection = url.openConnection().asInstanceOf[HttpURLConnection]
        connection.setDoOutput(true)
        connection.setRequestMethod("PUT")
        connection.addRequestProperty("Content-Type", "application/octet-stream")
        val out = new OutputStreamWriter(connection.getOutputStream)
        val partContents = "." * size
        out.write(partContents)
        out.close()
        val responseCode = connection.getResponseCode
        responseCode should ===(200)
        partContents
    }

    this.expectedContents = Some(parts.foldLeft("") { (acc, p) ⇒ acc + p })
  }

  def completeUpload() = {
    whenReady(service.handleCommitFileUpload(uploadKey, "/etc/passwd/The.Filë%00\u0000 – 'Fear and Loathing in Las Vegas'")) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseCommitFileUpload(_)) ⇒
      }

      this.fileLocation = Some(resp.toOption.get.uploadedFileLocation)
    }
  }

  def generateValidDownloadUrls() = {
    val urlStr = whenReady(service.handleGetFileUrl(fileLocation.get)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUrl(_, _, _, _)) ⇒
      }

      resp.toOption.get.url
    }

    urlStr should include("The.File%2500+-+'Fear+and+Loathing+in+Las+Vegas'?")
    urlStr shouldNot include("//The")
    urlStr shouldNot include("etc")
    urlStr shouldNot include("passwd")

    {
      val url = new URL(urlStr)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setDoOutput(true)
      connection.setRequestMethod("GET")
      connection.getResponseMessage should ===("OK")
      IOUtils.toString(connection.getInputStream) should ===(expectedContents.get)
    }

    checkRanged(urlStr, s"${expectedContents.get.length - 10}-", expectedContents.get.drop(expectedContents.get.length - 10))
    checkRanged(urlStr, s"10-13", expectedContents.get.slice(10, 14))
  }

  def validUploadPartUrlsDuplRequest() = {
    val partSize = 1024 * 32
    whenReady(service.handleGetFileUploadPartUrl(1, partSize, uploadKey)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUploadPartUrl(_)) ⇒
      }
    }
    whenReady(service.handleGetFileUploadPartUrl(1, partSize, uploadKey)) { resp ⇒
      resp should matchPattern {
        case Ok(ResponseGetFileUploadPartUrl(_)) ⇒
      }
    }
  }

  private def checkRanged(urlStr: String, range: String, expected: String): Unit = {
    val url = new URL(urlStr)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.setRequestMethod("GET")
    connection.setRequestProperty("Range", s"bytes=${range}")

    connection.getResponseMessage should ===("Partial Content")
    IOUtils.toString(connection.getInputStream) should ===(expected)
  }
}
