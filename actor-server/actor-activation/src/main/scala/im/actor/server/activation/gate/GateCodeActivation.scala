package im.actor.server.activation.gate

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ -\/, \/, \/- }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{ GET, POST }
import akka.http.scaladsl.model.{ HttpRequest, Uri }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import play.api.libs.json.Json
import slick.dbio.DBIO

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.internal.CodeActivation
import im.actor.server.activation.{ InvalidHash, ValidationResponse }
import im.actor.server.persist

class GateCodeActivation(config: GateConfig)(
  implicit
  system:           ActorSystem,
  val materializer: Materializer,
  ec:               ExecutionContext
) extends CodeActivation with JsonImplicits {

  private[this] val http = Http()

  override def send(optTransactionHash: Option[String], code: Code): DBIO[String \/ Unit] = {
    val codeResponse: Future[CodeResponse] = for {
      resp ← http.singleRequest(
        HttpRequest(
          method = POST,
          uri = s"${config.uri}/v1/codes/send",
          entity = Json.toJson(code).toString
        ).withHeaders(`X-Auth-Token`(config.authToken))
      )
      codeResp ← Unmarshal(resp).to[CodeResponse]
    } yield codeResp

    for {
      codeResponse ← DBIO.from(codeResponse)
      result ← codeResponse match {
        case CodeHash(hash) ⇒
          optTransactionHash.map { transactionHash ⇒
            for (_ ← persist.auth.GateAuthCode.create(transactionHash, hash)) yield \/-(())
          } getOrElse DBIO.successful(\/-(()))
        case CodeError(message) ⇒
          DBIO.successful(-\/(message))
      }
    } yield result
  }

  override def validate(transactionHash: String, code: String): DBIO[ValidationResponse] = {
    for {
      optCodeHash ← persist.auth.GateAuthCode.find(transactionHash)
      validationResponse ← DBIO.from(optCodeHash map { codeHash ⇒
        val validationUri = Uri(s"${config.uri}/v1/codes/validate/${codeHash.codeHash}").withQuery("code" → code)
        for {
          response ← http.singleRequest(HttpRequest(GET, validationUri).withHeaders(`X-Auth-Token`(config.authToken)))
          vr ← Unmarshal(response).to[ValidationResponse]
        } yield vr
      } getOrElse Future.successful(InvalidHash))
    } yield validationResponse
  }

  override def finish(transactionHash: String): DBIO[Unit] = persist.auth.GateAuthCode.delete(transactionHash).map(_ ⇒ ())
}