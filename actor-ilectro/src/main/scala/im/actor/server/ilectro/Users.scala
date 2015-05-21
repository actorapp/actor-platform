package im.actor.server.ilectro

import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods.{ DELETE, GET, POST }
import akka.http.scaladsl.model._
import akka.stream.ActorFlowMaterializer
import upickle._

import im.actor.server.ilectro.Common._
import im.actor.server.ilectro.results._

private[ilectro] class Users(implicit
  system: ActorSystem,
                             executionContext: ExecutionContext,
                             materializer:     ActorFlowMaterializer,
                             http:             HttpExt,
                             config:           ILectroConfig) {

  private implicit val authToken = config.authToken
  private val baseUrl = config.baseUrl

  private val resourceName = "users"

  def create(name: String): Future[Either[Errors, User]] = {
    val user = User(UUID.randomUUID(), name)
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName",
        entity = write(Data(user))
      ),
      success = (entity) ⇒ Future(Right(user)),
      failure = defaultFailure
    )
  }

  def getBanners(userUuid: UUID): Future[Either[Errors, Banner]] = processRequest(
    HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName/$userUuid/banners"
    ),
    success = (entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        val body = e.data.decodeString("utf-8")
        Right(read[Banner](body))
      },
    failure = defaultFailure
  )

  def getInterests(userUuid: UUID): Future[Either[Errors, List[Interest]]] = processRequest(
    HttpRequest(
      method = GET,
      uri = s"$baseUrl/$resourceName/$userUuid/interests"
    ),
    success = (entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        val body = e.data.decodeString("utf-8")
        Right(read[List[Interest]](body))
      },
    failure = defaultFailure
  )

  def deleteInterest(userUuid: UUID, interestId: Int): Future[Either[Errors, Unit]] = processRequest(
    HttpRequest(
      method = DELETE,
      uri = s"$baseUrl/$resourceName/$userUuid/interests/$interestId"
    ),
    success = (entity) ⇒ Future(Right(())),
    failure = defaultFailure
  )

  def addInterest(userUuid: UUID, interestIds: List[Int]): Future[Either[Errors, Unit]] = {
    val interests = s"{${interestIds mkString ","}}"
    processRequest(
      HttpRequest(
        method = POST,
        uri = s"$baseUrl/$resourceName/$userUuid/interests",
        entity = write(Data(interests))
      ),
      success = (entity) ⇒ Future(Right(())),
      failure = defaultFailure
    )
  }

}
