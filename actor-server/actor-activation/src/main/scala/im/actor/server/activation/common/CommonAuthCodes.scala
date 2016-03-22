package im.actor.server.activation.common

import java.time.temporal.ChronoUnit._
import java.time.{ LocalDateTime, ZoneOffset }

import cats.data.Xor
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.AuthCode
import im.actor.server.persist.AuthCodeRepo

import scala.concurrent.{ ExecutionContext, Future }

trait CommonAuthCodes {
  self: ActivationProvider ⇒

  protected val activationConfig: ActivationConfig
  protected val db: Database
  protected implicit val ec: ExecutionContext

  override def validate(txHash: String, code: String): Future[ValidationResponse] = {
    val action = for {
      optCode ← AuthCodeRepo.findByTransactionHash(txHash)
      result ← optCode map {
        case s if isExpired(s, activationConfig.expiration.toMillis) ⇒
          for (_ ← AuthCodeRepo.deleteByTransactionHash(txHash)) yield ExpiredCode
        case s if s.code != code ⇒
          if (s.attempts + 1 >= activationConfig.attempts) {
            for (_ ← AuthCodeRepo.deleteByTransactionHash(txHash)) yield ExpiredCode
          } else {
            for (_ ← AuthCodeRepo.incrementAttempts(txHash, s.attempts)) yield InvalidCode
          }
        case _ ⇒ DBIO.successful(Validated)
      } getOrElse DBIO.successful(InvalidHash)
    } yield result
    db.run(action)
  }

  protected def deleteAuthCode(txHash: String): Future[Unit] = db.run(AuthCodeRepo.deleteByTransactionHash(txHash).map(_ ⇒ ()))

  protected def createAuthCodeIfNeeded(resp: CodeFailure Xor Unit, txHash: String, code: String): Future[Int] = resp match {
    case Xor.Left(_)  ⇒ Future.successful(0)
    case Xor.Right(_) ⇒ db.run(AuthCodeRepo.createOrUpdate(txHash, code))
  }

  protected def isExpired(code: AuthCode, expiration: Long): Boolean =
    code.createdAt.plus(expiration, MILLIS).isBefore(LocalDateTime.now(ZoneOffset.UTC))

}
