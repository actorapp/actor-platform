package im.actor.server.llectro

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods.{ DELETE, GET, POST }
import akka.http.scaladsl.model._
import akka.stream.Materializer
import play.api.libs.json._

import im.actor.server.llectro.Common._
import im.actor.server.llectro.results._
import im.actor.server.models.llectro.{ Interest, LlectroUser }

private[llectro] class Users(
  implicit
  system:           ActorSystem,
  executionContext: ExecutionContext,
  materializer:     Materializer,
  http:             HttpExt,
  config:           LlectroConfig
) {
  import JsonFormatters._

  private implicit val authToken = config.authToken
  private val baseUrl = config.baseUrl

  private val resourceName = "users"

  def create(user: LlectroUser): Future[Either[Errors, LlectroUser]] = {
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName",
        entity = Json.stringify(dataObj(Json.toJson(user)))
      ),
      onSuccess = (entity) ⇒ Future(Right(user)),
      onFailure = defaultFailure
    )
  }

  def getBalance(userUuid: UUID): Future[Either[Errors, UserBalance]] = {
    processRequest(
      HttpRequest(
        method = GET,
        uri = s"$baseUrl/$resourceName/$userUuid"
      ),
      onSuccess = (entity) ⇒
        entity.toStrict(5.seconds).map { e ⇒
          Right(Json.parse(e.data.decodeString("utf-8")).validate[UserBalance].asOpt.get)
        },
      onFailure = defaultFailure
    )
  }

  def getBanners(userUuid: UUID, screenWidth: Int, screenHeight: Int): Future[Either[Errors, Banner]] = processRequest(
    HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName/$userUuid/banners?w=${screenWidth}"
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
    val interests = s"${interestIds mkString ","}"
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName/$userUuid/interests",
        entity = Json.stringify(Json.toJson(dataObj(interests)))
      ),
      onSuccess = (entity) ⇒ Future(Right(())),
      onFailure = defaultFailure
    )
  }

  def dataObj(str: String): JsObject = {
    JsObject(Seq("data" → JsString(str)))
  }

  def dataObj(value: JsValue): JsObject = {
    JsObject(Seq("data" → value))
  }
}
