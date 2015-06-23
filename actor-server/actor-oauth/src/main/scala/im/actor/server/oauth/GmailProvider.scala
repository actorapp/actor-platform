package im.actor.server.oauth

import java.net.URLEncoder

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ FormData, HttpRequest, RequestEntity, Uri }
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorFlowMaterializer
import java.time.{ ZoneOffset, LocalDateTime }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.server.{ models, persist }

case class Token(
  accessToken:  String,
  tokenType:    String,
  expiresIn:    Long,
  refreshToken: String,
  createdAt:    LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
)

class GmailProvider(gmailConfig: OAuth2GmailConfig)(
  implicit
  db:                   Database,
  system:               ActorSystem,
  ec:                   ExecutionContext,
  val flowMaterializer: ActorFlowMaterializer
) extends OAuth2Provider with Implicits {

  private val Utf8Encoding = "UTF-8"

  private val http = Http()

  def retreiveToken(code: String, clientName: String, redirectUri: Option[String]): DBIO[Option[models.OAuth2Token]] = {
    val form = FormData(
      "code" → code,
      "redirect_uri" → redirectUri.getOrElse(""),
      "client_id" → gmailConfig.clientId,
      "client_secret" → gmailConfig.clientSecret,
      "scope" → "",
      "grant_type" → "authorization_code"
    )
    for {
      optToken ← DBIO.from(makeRequest(form))
      result ← optToken.map { token ⇒
        val model = modelFromToken(token, clientName)
        for (_ ← persist.OAuth2Token.create(model)) yield Some(model)
      }.getOrElse(DBIO.successful(None))
    } yield result
  }

  def refreshToken(clientName: String): DBIO[Option[models.OAuth2Token]] = throw new Exception("Not implemented")

  //  def refreshToken(email: String): DBIO[Option[models.OAuth2Token]] = {
  //    for {
  //      optToken ← persist.OAuth2Token.findByEmail(email)
  //      result ← optToken.map { token ⇒
  //        val form = FormData(
  //          "client_id" → gmailConfig.clientId,
  //          "client_secret" → gmailConfig.clientSecret,
  //          "grant_type" → "refresh_token",
  //          "refresh_token" → token.refreshToken
  //        )
  //        for {
  //          token ← DBIO.from(makeRequest(form))
  //          model = modelFromToken(token, email)
  //          _ ← persist.OAuth2Token.create(model)
  //        } yield Right(model)
  //      } getOrElse DBIO.successful(None)
  //    } yield result
  //  }

  def getAuthUrl(redirectUrl: String, userId: String): Option[String] = {
    Try(Uri(redirectUrl)).map { _ ⇒
      s"""${gmailConfig.authUri}
          |?redirect_uri=${URLEncoder.encode(redirectUrl, Utf8Encoding)}
          |&response_type=code
          |&client_id=${gmailConfig.clientId}
          |&scope=${URLEncoder.encode(gmailConfig.scope, Utf8Encoding)}
          |&approval_prompt=force
          |&access_type=offline
          |&user_id=$userId""".stripMargin.replaceAll("\n", "")
    }.toOption
  }

  private def makeRequest(form: FormData): Future[Option[Token]] = for {
    entity ← Marshal(form).to[RequestEntity]
    response ← http.singleRequest(HttpRequest(POST, gmailConfig.tokenUri, entity = entity))
    token ← Unmarshal(response).to[Option[Token]]
  } yield token

  private def modelFromToken(token: Token, email: String): models.OAuth2Token = {
    val id = ThreadLocalRandom.current().nextLong()
    models.OAuth2Token(
      id = id,
      userId = email,
      accessToken = token.accessToken,
      tokenType = token.tokenType,
      expiresIn = token.expiresIn,
      refreshToken = token.refreshToken,
      createdAt = token.createdAt
    )
  }

}
