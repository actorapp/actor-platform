package im.actor.server.oauth

import java.net.URLEncoder
import java.time.temporal.ChronoUnit.SECONDS

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ FormData, HttpRequest, RequestEntity, Uri }
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import java.time.{ ZoneOffset, LocalDateTime }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.server.{ models, persist }

case class Token(
  accessToken:  String,
  tokenType:    String,
  expiresIn:    Long,
  refreshToken: Option[String],
  createdAt:    LocalDateTime  = LocalDateTime.now(ZoneOffset.UTC)
)

class GmailProvider(gmailConfig: OAuth2GmailConfig)(
  implicit
  db:               Database,
  system:           ActorSystem,
  ec:               ExecutionContext,
  val materializer: Materializer
) extends OAuth2Provider with Implicits {

  private val Utf8Encoding = "UTF-8"

  private val http = Http()

  def retreiveToken(code: String, userId: String, redirectUri: Option[String]): DBIO[Option[models.OAuth2Token]] = {
    for {
      optToken ← persist.OAuth2Token.findByUserId(userId)
      result ← optToken.map { token ⇒
        if (isExpired(token)) refreshToken(userId) else DBIO.successful(Some(token))
      } getOrElse {
        val form = FormData(
          "code" → code,
          "redirect_uri" → redirectUri.getOrElse(""),
          "client_id" → gmailConfig.clientId,
          "client_secret" → gmailConfig.clientSecret,
          "scope" → "",
          "grant_type" → "authorization_code"
        )
        fetchToken(form, userId)
      }
    } yield result
  }

  def refreshToken(userId: String): DBIO[Option[models.OAuth2Token]] = {
    for {
      optRefresh ← persist.OAuth2Token.findRefreshToken(userId)
      token ← optRefresh.map { refresh ⇒
        val form = FormData(
          "client_id" → gmailConfig.clientId,
          "client_secret" → gmailConfig.clientSecret,
          "grant_type" → "refresh_token",
          "refresh_token" → refresh.refreshToken.getOrElse("")
        )
        fetchToken(form, userId)
      }.getOrElse(DBIO.successful(None))
    } yield token
  }

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

  private def fetchToken(form: FormData, userId: String) = {
    for {
      optToken ← DBIO.from(makeRequest(form))
      result ← optToken.map { token ⇒
        val model = modelFromToken(token, userId)
        for (_ ← persist.OAuth2Token.create(model)) yield Some(model)
      }.getOrElse(DBIO.successful(None))
    } yield result
  }

  private def isExpired(token: models.OAuth2Token): Boolean =
    token.createdAt.plus(token.expiresIn, SECONDS).isBefore(LocalDateTime.now(ZoneOffset.UTC))

}
