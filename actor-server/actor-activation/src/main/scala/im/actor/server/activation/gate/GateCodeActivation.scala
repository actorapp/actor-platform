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
import slick.driver.PostgresDriver.api._

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.internal.CodeActivation
import im.actor.server.activation.{ InvalidHash, ValidationResponse }
import im.actor.server.persist

class GateCodeActivation(config: GateConfig)(
  implicit
  db:               Database,
  system:           ActorSystem,
  val materializer: Materializer,
  ec:               ExecutionContext
) extends CodeActivation with JsonImplicits {

  private[this] val http = Http()

  override def send(optTransactionHash: Option[String], code: Code): DBIO[String \/ Unit] =
    optTransactionHash.map { transactionHash ⇒
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

      val action = for {
        codeResponse ← DBIO.from(codeResponse)
        result ← codeResponse match {
          case CodeHash(hash) ⇒
            for (_ ← persist.auth.GateAuthCode.create(optTransactionHash.get, hash)) yield \/-(())
          case CodeError(message) ⇒
            DBIO.successful(-\/(message))
        }
      } yield result
      action
    } getOrElse (throw new Exception("transactionHash should be defined for new transaction methods"))

  override def validate(transactionHash: String, code: String): Future[ValidationResponse] = {
    for {
      optCodeHash ← db.run(persist.auth.GateAuthCode.find(transactionHash))
      validationResponse ← optCodeHash map { codeHash ⇒
        val validationUri = Uri(s"${config.uri}/v1/codes/validate/$codeHash").withQuery("code" → Json.toJson(code).toString)
        for {
          response ← http.singleRequest(HttpRequest(GET, validationUri).withHeaders(`X-Auth-Token`(config.authToken)))
          vr ← Unmarshal(response).to[ValidationResponse]
        } yield vr
      } getOrElse Future.successful(InvalidHash)
    } yield validationResponse
  }

  override def finish(transactionHash: String): DBIO[Unit] = persist.auth.GateAuthCode.delete(transactionHash).map(_ ⇒ ())
}