package im.actor.server.ilectro

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods.{ DELETE, GET, POST }
import akka.http.scaladsl.model._
import akka.stream.ActorFlowMaterializer
import play.api.libs.json._

import im.actor.server.ilectro.Common._
import im.actor.server.ilectro.results._
import im.actor.server.models.ilectro.{ Interest, ILectroUser }

private[ilectro] class Users(
  implicit
  system:           ActorSystem,
  executionContext: ExecutionContext,
  materializer:     ActorFlowMaterializer,
  http:             HttpExt,
  config:           ILectroConfig
) {
  import JsonFormatters._

  private implicit val authToken = config.authToken
  private val baseUrl = config.baseUrl

  private val resourceName = "users"

  def create(user: ILectroUser): Future[Either[Errors, ILectroUser]] = {
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName",
        entity = Json.stringify(Json.toJson(user))
      ),
      onSuccess = (entity) ⇒ Future(Right(user)),
      onFailure = defaultFailure
    )
  }

  def getBanners(userUuid: UUID): Future[Either[Errors, Banner]] = processRequest(
    HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName/$userUuid/banners"
    ),
    onSuccess = (entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        Right(Json.parse(e.data.decodeString("utf-8")).validate[Banner].asOpt.get)
      },
    onFailure = defaultFailure
  )

  def getInterests(userUuid: UUID): Future[Either[Errors, List[Interest]]] = processRequest(
    HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName/$userUuid/interests"
    ),
    onSuccess = (entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        Right(Json.parse(e.data.decodeString("utf-8")).validate[List[Interest]].asOpt.get)
      },
    onFailure = defaultFailure
  )

  def deleteInterest(userUuid: UUID, interestId: Int): Future[Either[Errors, Unit]] = processRequest(
    HttpRequest(
      method = DELETE,
      uri = s"$baseUrl/$resourceName/$userUuid/interests/$interestId"
    ),
    onSuccess = (entity) ⇒ Future(Right(())),
    onFailure = defaultFailure
  )

  def addInterests(userUuid: UUID, interestIds: List[Int]): Future[Either[Errors, Unit]] = {
    val interests = s"{${interestIds mkString ","}}"
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName/$userUuid/interests",
        entity = Json.stringify(Json.toJson(interests))
      ),
      onSuccess = (entity) ⇒ Future(Right(())),
      onFailure = defaultFailure
    )
  }

}
