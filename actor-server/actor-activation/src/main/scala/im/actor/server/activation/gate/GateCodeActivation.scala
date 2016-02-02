package im.actor.server.activation.gate

import akka.actor.ActorSystem
import akka.event.Logging
import im.actor.server.activation.Activation.Code
import im.actor.server.activation._
import im.actor.server.persist.auth.GateAuthCodeRepo
import slick.dbio.DBIO
import spray.client.pipelining._
import spray.http.HttpMethods.{ POST, GET }
import spray.http._
import spray.httpx.PlayJsonSupport
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._

import scala.concurrent.Future
import scala.reflect.ClassTag
import scalaz.{ -\/, \/, \/- }

class GateCodeActivation(config: GateConfig)(implicit system: ActorSystem) extends CodeActivation with JsonFormatters with PlayJsonSupport {
  import system.dispatcher

  private val log = Logging(system, getClass)

  val pipeline: HttpRequest ⇒ Future[HttpResponse] = addHeader("X-Auth-Token", config.authToken) ~> sendReceive

  override def send(optTransactionHash: Option[String], code: Code): DBIO[CodeFailure \/ Unit] = {
    val codeResponse: Future[CodeResponse] = for {
      entity ← marshalToEntity(code)
      request = HttpRequest(method = POST, uri = s"${config.uri}/v1/codes/send", entity = entity)
      _ = log.debug("Requesting code send with {}", request)
      resp ← pipeline(request)
      codeResp ← unmarshal[CodeResponse](resp)
    } yield codeResp

    for {
      codeResponse ← DBIO.from(codeResponse)
      result ← codeResponse match {
        case CodeHash(hash) ⇒
          optTransactionHash.map { transactionHash ⇒
            for (_ ← GateAuthCodeRepo.createOrUpdate(transactionHash, hash)) yield \/-(())
          } getOrElse DBIO.successful(\/-(()))
        case failure: CodeFailure ⇒
          DBIO.successful(-\/(failure))
      }
    } yield result
  }

  override def validate(transactionHash: String, code: String): DBIO[ValidationResponse] = {
    for {
      optCodeHash ← GateAuthCodeRepo.find(transactionHash)
      validationResponse ← DBIO.from(optCodeHash map { codeHash ⇒
        val validationUri = Uri(s"${config.uri}/v1/codes/validate/${codeHash.codeHash}").withQuery("code" → code)
        val request = HttpRequest(GET, validationUri)
        log.debug("Requesting code validation with {}", request)

        for {
          response ← pipeline(request)
          vr ← unmarshal[ValidationResponse](response)
        } yield vr
      } getOrElse Future.successful(InvalidHash))
    } yield validationResponse
  }

  override def finish(transactionHash: String): DBIO[Unit] = GateAuthCodeRepo.delete(transactionHash).map(_ ⇒ ())

  private def marshalToEntity[T: ClassTag](value: T)(implicit marshaller: Marshaller[T]): Future[HttpEntity] =
    marshal[T](value) match {
      case Left(e) ⇒
        log.warning("Failed to marshal value: {}", e)
        Future.failed(e)
      case Right(entity) ⇒ Future.successful(entity)
    }

  private def unmarshal[T: ClassTag](response: HttpResponse)(implicit um: FromResponseUnmarshaller[T]): Future[T] =
    response.as[T] match {
      case Left(e)       ⇒ Future.failed(new Exception(s"Failed to parse json: ${response.entity.asString}"))
      case Right(result) ⇒ Future.successful(result)
    }

}