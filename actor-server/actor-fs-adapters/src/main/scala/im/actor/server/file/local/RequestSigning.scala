package im.actor.server.file.local

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpMethod, Uri }
import im.actor.server.acl.ACLUtils
import org.apache.commons.codec.digest.DigestUtils.sha256Hex
import org.apache.commons.codec.digest.HmacUtils

trait RequestSigning {

  private val urlEncode: String ⇒ String = URLEncoder.encode(_, "UTF-8")

  def signRequest(httpVerb: HttpMethod, uri: Uri, secret: String): Uri =
    uri.withQuery(("signature" → calculateSignature(httpVerb, uri, secret)) +: uri.query())

  def calculateSignature(httpVerb: HttpMethod, uri: Uri)(implicit system: ActorSystem): String =
    calculateSignature(httpVerb, uri, ACLUtils.secretKey())

  def calculateSignature(httpVerb: HttpMethod, uri: Uri, secret: String): String = {
    val canonicalUri = urlEncode(uri.withQuery(Uri.Query.Empty).toString)

    val canonicalQueryString = uri.query() sortBy (_._1) map {
      case (name, value) ⇒
        s"${urlEncode(name)}=${urlEncode(value)}"
    } mkString "&"

    val canonicalRequest =
      s"""$httpVerb
         |$canonicalUri
         |$canonicalQueryString""".stripMargin

    HmacUtils.hmacSha256Hex(secret, sha256Hex(canonicalRequest))
  }
}