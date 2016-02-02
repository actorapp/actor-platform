package im.actor.server.oauth

import java.net.URLEncoder
import java.time.temporal.ChronoUnit.SECONDS
import java.time.{ LocalDateTime, ZoneOffset }

import im.actor.server.persist.OAuth2TokenRepo

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import slick.dbio.DBIO

import im.actor.server.model.OAuth2Token

class GoogleProvider(googleConfig: OAuth2GoogleConfig)(
  implicit
  system:           ActorSystem,
  val materializer: Materializer
) extends OAuth2Provider with Implicits {

  implicit val ec: ExecutionContext = system.dispatcher

  private val Utf8Encoding = "UTF-8"

  private val http = Http()

  def completeOAuth(code: String, userId: String, redirectUri: Option[String]): DBIO[Option[OAuth2Token]] = {
    for {
      optToken ← OAuth2TokenRepo.findByUserId(userId)
      result ← optToken.map { token ⇒
        if (isExpired(token)) refreshToken(userId) else DBIO.successful(Some(token))
      } getOrElse getTokenFirstTime(code, userId, redirectUri)
    } yield result
  }

  def refreshToken(userId: String): DBIO[Option[OAuth2Token]] = {
    for {
      optRefresh ← OAuth2TokenRepo.findRefreshToken(userId)
      token ← optRefresh.map { refresh ⇒
        val form = FormData(
          "client_id" → googleConfig.clientId,
          "client_secret" → googleConfig.clientSecret,
          "grant_type" → "refresh_token",
          "refresh_token" → refresh.refreshToken.getOrElse("")
        )
        DBIO.from(fetchToken(form, userId))
      }.getOrElse(DBIO.successful(None))
    } yield token
  }

  def getAuthUrl(redirectUrl: String, userId: String): Option[String] = {
    Try(Uri(redirectUrl)).map { _ ⇒
      s"""${googleConfig.authUri}
          |?redirect_uri=${URLEncoder.encode(redirectUrl, Utf8Encoding)}
          |&response_type=code
          |&client_id=${googleConfig.clientId}
          |&scope=${URLEncoder.encode(googleConfig.scope, Utf8Encoding)}
          |&approval_prompt=force
          |&access_type=offline
          |&user_id=$userId""".stripMargin.replaceAll("\n", "")
    }.toOption
  }

  def fetchProfile(accessToken: String): Future[Option[Profile]] = {
    for {
      response ← http.singleRequest(HttpRequest(GET, uri = googleConfig.profileUri, headers = List(Authorization(OAuth2BearerToken(accessToken)))))
      profile ← Unmarshal(response).to[Option[Profile]]
    } yield profile
  }

  private def getTokenFirstTime(code: String, userId: String, redirectUri: Option[String]): DBIO[Option[OAuth2Token]] = {
    val form = FormData(
      "code" → code,
      "redirect_uri" → redirectUri.getOrElse(""),
      "client_id" → googleConfig.clientId,
      "client_secret" → googleConfig.clientSecret,
      "scope" → "",
      "grant_type" → "authorization_code"
    )
    DBIO.from(fetchToken(form, userId))
  }

  private def fetchToken(form: FormData, userId: String): Future[Option[OAuth2Token]] =
    for {
      optToken ← requestToken(form)
      result ← Future.successful(optToken.map(makeModel(_, userId)))
    } yield result

  private def requestToken(form: FormData): Future[Option[Token]] = for {
    entity ← Marshal(form).to[RequestEntity]
    response ← http.singleRequest(HttpRequest(POST, googleConfig.tokenUri, entity = entity))
    token ← Unmarshal(response).to[Option[Token]]
  } yield token

  private def makeModel(token: Token, email: String): OAuth2Token = {
    val id = ThreadLocalRandom.current().nextLong()
    OAuth2Token(
      id = id,
      userId = email,
      accessToken = token.accessToken,
      tokenType = token.tokenType,
      expiresIn = token.expiresIn,
      refreshToken = token.refreshToken,
      createdAt = token.createdAt
    )
  }

  private def isExpired(token: OAuth2Token): Boolean =
    token.createdAt.plus(token.expiresIn, SECONDS).isBefore(LocalDateTime.now(ZoneOffset.UTC))

}
