package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import im.actor.server.acl.ACLUtils

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scalaz.{ -\/, \/, \/- }

import akka.actor.ActorSystem
import akka.pattern.ask
import slick.dbio._

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.users.ApiSex._
import im.actor.server.activation.Activation.{ EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.models.{ AuthEmailTransaction, AuthPhoneTransaction, User }
import im.actor.server.persist.auth.AuthTransaction
import im.actor.server.session._
import im.actor.server.user.UserOffice
import im.actor.util.misc.IdUtils._
import im.actor.util.misc.PhoneNumberUtils._
import im.actor.util.misc.StringUtils.validName
import im.actor.server.{ models, persist }

trait AuthHelpers extends Helpers {
  self: AuthServiceImpl ⇒

  //expiration of code won't work
  protected def newUserPhoneSignUp(transaction: models.AuthPhoneTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) \/ User] = {
    val phone = transaction.phoneNumber
    for {
      optPhone ← fromDBIO(persist.UserPhone.findByPhoneNumber(phone).headOption)
      phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
      (_, countryCode) = phoneAndCode
      result ← optPhone match {
        case Some(userPhone) ⇒ point(-\/((userPhone.userId, countryCode)))
        case None            ⇒ newUser(name, countryCode, sex)
      }
    } yield result
  }

  protected def newUserEmailSignUp(transaction: models.AuthEmailTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) \/ User] = {
    val email = transaction.email
    for {
      optEmail ← fromDBIO(persist.UserEmail.find(email))
      result ← optEmail match {
        case Some(existingEmail) ⇒ point(-\/((existingEmail.userId, "")))
        case None ⇒
          val userResult: Result[(Int, String) \/ User] =
            for {
              optToken ← fromDBIO(persist.OAuth2Token.findByUserId(email))
              locale ← optToken.map { token ⇒
                val locale = oauth2Service.fetchProfile(token.accessToken).map(_.flatMap(_.locale))
                fromFuture(locale)
              }.getOrElse(point(None))
              user ← newUser(name, locale.getOrElse("").toUpperCase, sex)
            } yield user
          userResult
      }
    } yield result
  }

  def handleUserCreate(user: models.User, transaction: models.AuthTransactionChildren, authId: Long): Result[User] = {
    for {
      _ ← fromFuture(UserOffice.create(user.id, user.accessSalt, user.name, user.countryCode, im.actor.api.rpc.users.ApiSex(user.sex.toInt), isBot = false))
      _ ← fromDBIO(persist.AvatarData.create(models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong)))
      _ ← fromDBIO(AuthTransaction.delete(transaction.transactionHash))
      _ ← transaction match {
        case p: models.AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            _ ← fromDBIO(activationContext.finish(p.transactionHash))
            _ ← fromFuture(UserOffice.addPhone(user.id, phone))
          } yield ()
        case e: models.AuthEmailTransaction ⇒
          fromFuture(UserOffice.addEmail(user.id, e.email))
      }
    } yield user
  }

  /**
   * Validate phone code and remove `AuthCode` and `AuthTransaction`
   * used for this sign action.
   */
  protected def validateCode(transaction: models.AuthTransactionChildren, code: String): Result[(Int, String)] = {
    val (codeExpired, codeInvalid) = transaction match {
      case _: AuthPhoneTransaction ⇒ (AuthErrors.PhoneCodeExpired, AuthErrors.PhoneCodeInvalid)
      case _: AuthEmailTransaction ⇒ (AuthErrors.EmailCodeExpired, AuthErrors.EmailCodeInvalid)
    }
    val transactionHash = transaction.transactionHash
    for {
      validationResponse ← fromDBIO(activationContext.validate(transactionHash, code))
      _ ← validationResponse match {
        case ExpiredCode     ⇒ cleanupAndError(transactionHash, codeExpired)
        case InvalidHash     ⇒ cleanupAndError(transactionHash, AuthErrors.InvalidAuthCodeHash)
        case InvalidCode     ⇒ fromEither[Unit](-\/(codeInvalid))
        case InvalidResponse ⇒ cleanupAndError(transactionHash, AuthErrors.ActivationServiceError)
        case Validated       ⇒ point(())
      }
      _ ← fromDBIO(persist.auth.AuthTransaction.updateSetChecked(transactionHash))

      userAndCountry ← transaction match {
        case p: AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            //if user is not registered - return error
            phoneModel ← fromDBIOOption(AuthErrors.PhoneNumberUnoccupied)(persist.UserPhone.findByPhoneNumber(phone).headOption)
            phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
            _ ← fromDBIO(activationContext.finish(transactionHash))
          } yield (phoneModel.userId, phoneAndCode._2)
        case e: AuthEmailTransaction ⇒
          for {
            //if user is not registered - return error
            emailModel ← fromDBIOOption(AuthErrors.EmailUnoccupied)(persist.UserEmail.find(e.email))
            _ ← fromDBIO(activationContext.finish(transactionHash))
          } yield (emailModel.userId, "")
      }
    } yield userAndCountry
  }

  /**
   * Terminate all sessions associated with given `deviceHash`
   * and create new session
   */
  protected def refreshAuthSession(deviceHash: Array[Byte], newSession: models.AuthSession): DBIO[Unit] =
    for {
      prevSessions ← persist.AuthSession.findByDeviceHash(deviceHash)
      _ ← DBIO.from(Future.sequence(prevSessions map UserOffice.logout))
      _ ← persist.AuthSession.create(newSession)
    } yield ()

  protected def authorize(userId: Int, clientData: ClientData)(
    implicit
    sessionRegion: SessionRegion
  ): Future[AuthorizeUserAck] = {
    for {
      _ ← UserOffice.auth(userId, clientData.authId)
      ack ← sessionRegion.ref
        .ask(SessionEnvelope(clientData.authId, clientData.sessionId).withAuthorizeUser(AuthorizeUser(userId)))
        .mapTo[AuthorizeUserAck]
    } yield ack
  }

  //TODO: what country to use in case of email auth
  protected def authorizeT(userId: Int, countryCode: String, clientData: ClientData): Result[User] = {
    for {
      user ← fromDBIOOption(CommonErrors.UserNotFound)(persist.User.find(userId).headOption)
      _ ← fromFuture(UserOffice.changeCountryCode(userId, countryCode))
      _ ← fromDBIO(persist.AuthId.setUserData(clientData.authId, userId))
    } yield user
  }

  protected def sendSmsCode(phoneNumber: Long, code: String, transactionHash: Option[String])(implicit system: ActorSystem): DBIO[String \/ Unit] = {
    log.info("Sending code {} to {}", code, phoneNumber)
    activationContext.send(transactionHash, SmsCode(phoneNumber, code))
  }

  protected def sendEmailCode(email: String, code: String, transactionHash: String)(implicit system: ActorSystem): DBIO[String \/ Unit] = {
    log.info("Sending code {} to {}", code, email)
    activationContext.send(Some(transactionHash), EmailCode(email, code))
  }

  protected def genCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(6)

  protected def genSmsHash() = ThreadLocalRandom.current.nextLong().toString

  protected def genSmsCode(phone: Long): String = phone.toString match {
    case strNumber if strNumber.startsWith("7555") ⇒ strNumber(4).toString * 4
    case _                                         ⇒ genCode()
  }

  private def newUser(name: String, countryCode: String, optSex: Option[ApiSex]): Result[\/-[User]] = {
    val rng = ThreadLocalRandom.current()
    val sex = optSex.map(s ⇒ models.Sex.fromInt(s.id)).getOrElse(models.NoSex)
    for {
      validName ← fromEither(validName(name).leftMap(validationFailed("NAME_INVALID", _)))
      user = models.User(
        id = nextIntId(rng),
        accessSalt = ACLUtils.nextAccessSalt(rng),
        name = validName,
        countryCode = countryCode,
        sex = sex,
        state = models.UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC)
      )
    } yield \/-(user)
  }

  private def cleanupAndError(transactionHash: String, error: RpcError): Result[Unit] = {
    for {
      _ ← fromDBIO(persist.auth.AuthTransaction.delete(transactionHash))
      _ ← fromEither[Unit](Error(error))
    } yield ()
  }

}
