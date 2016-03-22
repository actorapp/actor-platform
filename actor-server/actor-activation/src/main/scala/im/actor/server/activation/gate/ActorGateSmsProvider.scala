package im.actor.server.activation.gate

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.persist.auth.GateAuthCodeRepo
import spray.client.pipelining._
import spray.http.HttpMethods.{ GET, POST }
import spray.http._
import spray.httpx.PlayJsonSupport
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._

import scala.concurrent.Future
import scala.reflect.ClassTag

private[activation] final class ActorGateSmsProvider(implicit system: ActorSystem)
  extends ActivationProvider
  with JsonFormatters
  with PlayJsonSupport {

  private val log = Logging(system, getClass)

  private val config = GateConfig.load.getOrElse(throw new RuntimeException("Failed to load activation gate config"))
  private val db = DbExtension(system).db
  protected implicit val ec = system.dispatcher

  val pipeline: HttpRequest ⇒ Future[HttpResponse] = addHeader("X-Auth-Token", config.authToken) ~> sendReceive

  // it would be better to have trait SmsProvider with send method for SmsCode only. In this case we won't have require()
  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = {
    require(code.isInstanceOf[SmsCode], "ActorGateSmsProvider is only capable of processing sms codes")

    val codeSendRequest: Future[CodeResponse] = for {
      entity ← marshalToEntity(code)
      request = HttpRequest(method = POST, uri = s"${config.uri}/v1/codes/send", entity = entity)
      _ = log.debug("Requesting code send with {}", request)
      resp ← pipeline(request)
      codeResp ← if (resp.status.isFailure) {
        FastFuture.successful(SendFailure(resp.entity.asString))
      } else {
        unmarshal[CodeResponse](resp)
      }
    } yield codeResp

    for {
      codeResponse ← codeSendRequest
      result ← codeResponse match {
        case CodeHash(codeHash)   ⇒ for (_ ← db.run(GateAuthCodeRepo.createOrUpdate(txHash, codeHash))) yield Xor.right(())
        case failure: CodeFailure ⇒ FastFuture.successful(Xor.left(failure))
      }
    } yield result
  }

  override def validate(txHash: String, code: String): Future[ValidationResponse] = {
    for {
      optCodeHash ← db.run(GateAuthCodeRepo.find(txHash))
      validationResponse ← optCodeHash map { codeHash ⇒
        val validationUri = Uri(s"${config.uri}/v1/codes/validate/${codeHash.codeHash}").withQuery("code" → code)
        val request = HttpRequest(GET, validationUri)
        log.debug("Requesting code validation with {}", request)

        for {
          response ← pipeline(request)
          vr ← unmarshal[ValidationResponse](response)
        } yield vr
      } getOrElse FastFuture.successful(InvalidHash)
    } yield validationResponse
  }

  override def cleanup(txHash: String): Future[Unit] = db.run(GateAuthCodeRepo.delete(txHash)).map(_ ⇒ ())

  private def marshalToEntity[T: ClassTag](value: T)(implicit marshaller: Marshaller[T]): Future[HttpEntity] =
    marshal[T](value) match {
      case Left(e) ⇒
        log.warning("Failed to marshal value: {}", e)
        Future.failed(e)
      case Right(entity) ⇒ FastFuture.successful(entity)
    }

  private def unmarshal[T: ClassTag](response: HttpResponse)(implicit um: FromResponseUnmarshaller[T]): Future[T] =
    response.as[T] match {
      case Left(e)       ⇒ FastFuture.failed(new Exception(s"Failed to parse json: ${response.entity.asString}"))
      case Right(result) ⇒ FastFuture.successful(result)
    }

}