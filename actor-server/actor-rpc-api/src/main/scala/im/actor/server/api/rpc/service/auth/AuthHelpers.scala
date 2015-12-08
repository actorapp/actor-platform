package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.pattern.ask
import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.users.ApiSex._
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.Activation.{ CallCode, EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.auth.DeviceInfo
import im.actor.server.model.{ AuthEmailTransaction, AuthPhoneTransaction, User }
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.session._
import im.actor.server.{ model, persist }
import im.actor.util.misc.EmailUtils.isTestEmail
import im.actor.util.misc.IdUtils._
import im.actor.util.misc.PhoneNumberUtils._
import im.actor.util.misc.StringUtils.validName
import slick.dbio._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Try
import scalaz.{ -\/, \/, \/- }

trait AuthHelpers extends Helpers {
  self: AuthServiceImpl ⇒

  //expiration of code won't work
  protected def newUserPhoneSignUp(transaction: model.AuthPhoneTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) \/ User] = {
    val phone = transaction.phoneNumber
    for {
      optPhone ← fromDBIO(persist.UserPhoneRepo.findByPhoneNumber(phone).headOption)
      phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
      (_, countryCode) = phoneAndCode
      result ← optPhone match {
        case Some(userPhone) ⇒ point(-\/((userPhone.userId, countryCode)))
        case None            ⇒ newUser(name, countryCode, sex)
      }
    } yield result
  }

  protected def newUserEmailSignUp(transaction: model.AuthEmailTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) \/ User] = {
    val email = transaction.email
    for {
      optEmail ← fromDBIO(persist.UserEmailRepo.find(email))
      result ← optEmail match {
        case Some(existingEmail) ⇒ point(-\/((existingEmail.userId, "")))
        case None ⇒
          val userResult: Result[(Int, String) \/ User] =
            for {
              optToken ← fromDBIO(persist.OAuth2TokenRepo.findByUserId(email))
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

  def handleUserCreate(user: model.User, transaction: model.AuthTransactionChildren): Result[User] = {
    for {
      _ ← fromFuture(userExt.create(user.id, user.accessSalt, user.nickname, user.name, user.countryCode, im.actor.api.rpc.users.ApiSex(user.sex.toInt), isBot = false))
      _ ← fromFuture(userExt.setDeviceInfo(user.id, DeviceInfo.parseFrom(transaction.deviceInfo)) recover { case _ ⇒ () })
      _ ← fromDBIO(persist.AvatarDataRepo.create(model.AvatarData.empty(model.AvatarData.OfUser, user.id.toLong)))
      _ ← fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
      _ ← transaction match {
        case p: model.AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            _ ← fromDBIO(activationContext.finish(p.transactionHash))
            _ ← fromFuture(userExt.addPhone(user.id, phone))
          } yield ()
        case e: model.AuthEmailTransaction ⇒
          fromFuture(userExt.addEmail(user.id, e.email))
      }
    } yield user
  }

  /**
   * Validate phone code and remove `AuthCode` and `AuthTransaction`
   * used for this sign action.
   */
  protected def validateCode(transaction: model.AuthTransactionChildren, code: String): Result[(Int, String)] = {
    val (codeExpired, codeInvalid) = transaction match {
      case _: AuthPhoneTransaction ⇒ (AuthErrors.PhoneCodeExpired, AuthErrors.PhoneCodeInvalid)
      case _: AuthEmailTransaction ⇒ (AuthErrors.EmailCodeExpired, AuthErrors.EmailCodeInvalid)
    }
    val transactionHash = transaction.transactionHash
    for {
      validationResponse ← fromDBIO(activationContext.validate(transactionHash, code))
      _ ← validationResponse match {
        case ExpiredCode                     ⇒ cleanupAndError(transactionHash, codeExpired)
        case InvalidHash                     ⇒ cleanupAndError(transactionHash, AuthErrors.InvalidAuthCodeHash)
        case InvalidCode                     ⇒ fromEither[Unit](-\/(codeInvalid))
        case InvalidResponse | InternalError ⇒ cleanupAndError(transactionHash, AuthErrors.ActivationServiceError)
        case Validated                       ⇒ point(())
      }
      _ ← fromDBIO(persist.auth.AuthTransactionRepo.updateSetChecked(transactionHash))

      userAndCountry ← transaction match {
        case p: AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            //if user is not registered - return error
            phoneModel ← fromDBIOOption(AuthErrors.PhoneNumberUnoccupied)(persist.UserPhoneRepo.findByPhoneNumber(phone).headOption)
            phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
            _ ← fromDBIO(activationContext.finish(transactionHash))
          } yield (phoneModel.userId, phoneAndCode._2)
        case e: AuthEmailTransaction ⇒
          for {
            //if user is not registered - return error
            emailModel ← fromDBIOOption(AuthErrors.EmailUnoccupied)(persist.UserEmailRepo.find(e.email))
            _ ← fromDBIO(activationContext.finish(transactionHash))
          } yield (emailModel.userId, "")
      }
    } yield userAndCountry
  }

  /**
   * Terminate all sessions associated with given `deviceHash`
   * and create new session
   */
  protected def refreshAuthSession(deviceHash: Array[Byte], newSession: model.AuthSession): DBIO[Unit] =
    for {
      // prevSessions ← persist.AuthSessionRepo.findByDeviceHash(deviceHash)
      //_ ← DBIO.from(Future.sequence(prevSessions map userExt.logout))
      _ ← persist.AuthSessionRepo.create(newSession)
    } yield ()

  protected def authorize(userId: Int, clientData: ClientData)(implicit sessionRegion: SessionRegion): Future[AuthorizeUserAck] = {
    for {
      _ ← userExt.auth(userId, clientData.authId)
      ack ← sessionRegion.ref
        .ask(SessionEnvelope(clientData.authId, clientData.sessionId).withAuthorizeUser(AuthorizeUser(userId)))
        .mapTo[AuthorizeUserAck]
    } yield ack
  }

  //TODO: what country to use in case of email auth
  protected def authorizeT(userId: Int, countryCode: String, deviceInfo: DeviceInfo, clientData: ClientData): Result[User] = {
    for {
      user ← fromDBIOOption(CommonErrors.UserNotFound)(persist.UserRepo.find(userId).headOption)
      _ ← fromFuture(userExt.changeCountryCode(userId, countryCode))
      _ ← fromFuture(userExt.setDeviceInfo(userId, deviceInfo) recover { case _ ⇒ () })
      _ ← fromDBIO(persist.AuthIdRepo.setUserData(clientData.authId, userId))
    } yield user
  }

  protected def sendSmsCode(phoneNumber: Long, code: String, transactionHash: Option[String])(implicit system: ActorSystem): DBIO[CodeFailure \/ Unit] = {
    log.info("Sending sms code {} to {}", code, phoneNumber)
    activationContext.send(transactionHash, SmsCode(phoneNumber, code))
  }

  protected def sendCallCode(phoneNumber: Long, code: String, transactionHash: Option[String], language: String)(implicit system: ActorSystem): DBIO[CodeFailure \/ Unit] = {
    log.info("Sending call code {} to {}", code, phoneNumber)
    activationContext.send(transactionHash, CallCode(phoneNumber, code, language))
  }

  protected def sendEmailCode(email: String, code: String, transactionHash: String)(implicit system: ActorSystem): DBIO[CodeFailure \/ Unit] = {
    log.info("Sending email code {} to {}", code, email)
    activationContext.send(Some(transactionHash), EmailCode(email, code))
  }

  protected def genSmsHash() = ThreadLocalRandom.current.nextLong().toString

  protected def genEmailCode(email: String): String =
    if (isTestEmail(email)) genTestCode(email) else genCode()

  protected def genSmsCode(phone: Long): String = phone.toString match {
    case strNumber if isTestPhone(phone) ⇒ Try(strNumber(4).toString * 4).getOrElse(phone.toString)
    case _                               ⇒ genCode()
  }

  private def newUser(name: String, countryCode: String, optSex: Option[ApiSex]): Result[\/-[User]] = {
    val rng = ThreadLocalRandom.current()
    val sex = optSex.map(s ⇒ model.Sex.fromInt(s.id)).getOrElse(model.NoSex)
    for {
      validName ← fromEither(validName(name).leftMap(validationFailed("NAME_INVALID", _)))
      user = model.User(
        id = nextIntId(rng),
        accessSalt = ACLUtils.nextAccessSalt(rng),
        name = validName,
        countryCode = countryCode,
        sex = sex,
        state = model.UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC),
        external = None
      )
    } yield \/-(user)
  }

  private def cleanupAndError(transactionHash: String, error: RpcError): Result[Unit] = {
    for {
      _ ← fromDBIO(persist.auth.AuthTransactionRepo.delete(transactionHash))
      _ ← fromEither[Unit](Error(error))
    } yield ()
  }

  private def genTestCode(email: String): String =
    (email replaceAll (""".*acme""", "")) replaceAll (".com", "")

  private def genCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(5)

}
