package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.pattern.ask
import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.users.ApiSex._
import im.actor.api.rpc.users.ApiUser
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.Activation.{ CallCode, EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.api.rpc.service.profile.ProfileErrors
import im.actor.server.auth.DeviceInfo
import im.actor.server.model._
import im.actor.server.persist.UserRepo
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.session._
import im.actor.server.{ model, persist }
import im.actor.util.misc.EmailUtils.isTestEmail
import im.actor.util.misc.IdUtils._
import im.actor.util.misc.PhoneNumberUtils._
import im.actor.util.misc.StringUtils.validName
import org.joda.time.DateTime
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
        case None            ⇒ newUser(name, countryCode, sex, username = None)
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
              user ← newUser(name, locale.getOrElse("").toUpperCase, sex, username = None)
            } yield user
          userResult
      }
    } yield result
  }

  protected def newUsernameSignUp(transaction: AuthUsernameTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) \/ User] = {
    val username = transaction.username
    for {
      optUser ← fromDBIO(UserRepo.findByNickname(username))
      result ← optUser match {
        case Some(existingUser) ⇒ point(-\/((existingUser.id, "")))
        case None               ⇒ newUser(name, "", sex, username = Some(username))
      }
    } yield result
  }

  protected def handleUserCreate(user: model.User, transaction: model.AuthTransactionBase, clientData: ClientData): Result[Unit] = {
    for {
      _ ← fromFuture(userExt.create(user.id, user.accessSalt, user.nickname, user.name, user.countryCode, im.actor.api.rpc.users.ApiSex(user.sex.toInt), isBot = false))
      _ ← fromDBIO(persist.AvatarDataRepo.create(model.AvatarData.empty(model.AvatarData.OfUser, user.id.toLong)))
      _ ← transaction match {
        case p: model.AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            _ ← fromDBIO(activationContext.finish(p.transactionHash))
            _ ← fromFuture(userExt.addPhone(user.id, phone))
          } yield ()
        case e: model.AuthEmailTransaction ⇒
          for {
            _ ← fromDBIO(activationContext.finish(e.transactionHash))
            _ ← fromFuture(userExt.addEmail(user.id, e.email))
          } yield ()
        case u: AuthUsernameTransaction ⇒
          fromFuture(userExt.changeNickname(user.id, Some(u.username)))
        case u: AuthAnonymousTransaction ⇒
          fromFuture(userExt.changeNickname(user.id, Some(u.username)))
      }
    } yield ()
  }

  /**
   * Hacky helper
   *
   * @param transaction
   * @return Right((codeExpiredError, codeInvalidError)) or Left(authUsernameTransaction) if it's a username transaction
   */
  private def expirationErrors(transaction: AuthTransactionBase with ExpirableCode) =
    transaction match {
      case _: AuthPhoneTransaction ⇒ (AuthErrors.PhoneCodeExpired, AuthErrors.PhoneCodeInvalid)
      case _: AuthEmailTransaction ⇒ (AuthErrors.EmailCodeExpired, AuthErrors.EmailCodeInvalid)
    }

  /**
   * Validate phone code and remove `AuthCode` and `AuthTransaction`
   * used for this sign action.
   */
  protected def validateCode(transaction: model.AuthTransactionBase, code: String): Result[(Int, String)] = {
    val transactionHash = transaction.transactionHash
    for {
      _ ← transaction match {
        case tx: AuthTransactionBase with ExpirableCode ⇒
          val (codeExpired, codeInvalid) = expirationErrors(tx)
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
          } yield ()
        case tx: AuthUsernameTransaction ⇒
          tx.userId match {
            case Some(userId) if !tx.isChecked ⇒
              for {
                _ ← fromDBIOBoolean(AuthErrors.PasswordInvalid)(ACLUtils.checkPassword(userId, code))
                _ ← fromDBIO(AuthTransactionRepo.updateSetChecked(transactionHash))
              } yield ()
            case None if tx.isChecked ⇒ point(DBIO.successful(()))
            // The following cases should never happen 'cause we set isChecked only if user is unregistered
            case Some(userId) if tx.isChecked ⇒
              log.error("AuthUsernameTransaction with userId {} is already checked")
              point(DBIO.successful(AuthErrors.PasswordInvalid))
            case None if !tx.isChecked ⇒
              log.error("AuthUsernameTransaction with not set userId and not checked")
              point(DBIO.successful(AuthErrors.PasswordInvalid))
          }
        case tx: AuthAnonymousTransaction ⇒ fromEither(-\/(AuthErrors.NotValidated))
      }

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
        case u: AuthUsernameTransaction ⇒
          for {
            userModel ← fromDBIOOption(AuthErrors.UsernameUnoccupied)(UserRepo.findByNickname(u.username))
          } yield (userModel.id, "")
        case _: AuthAnonymousTransaction ⇒
          fromEither(-\/(AuthErrors.NotValidated))
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
  protected def authorizeT(
    userId:      Int,
    countryCode: String,
    transaction: AuthTransactionBase,
    clientData:  ClientData
  ): Result[ApiUser] = {
    for {
      _ ← fromFuture(if (countryCode.nonEmpty) userExt.changeCountryCode(userId, countryCode) else Future.successful(()))
      _ ← fromFuture(userExt.setDeviceInfo(userId, DeviceInfo.parseFrom(transaction.deviceInfo)) recover { case _ ⇒ () })
      _ ← fromDBIO(persist.AuthIdRepo.setUserData(clientData.authId, userId))
      userStruct ← fromFuture(userExt.getApiStruct(userId, userId, clientData.authId))
      //refresh session data
      authSession = model.AuthSession(
        userId = userId,
        id = nextIntId(ThreadLocalRandom.current()),
        authId = clientData.authId,
        appId = transaction.appId,
        appTitle = model.AuthSession.appTitleOf(transaction.appId),
        deviceHash = transaction.deviceHash,
        deviceTitle = transaction.deviceTitle,
        authTime = DateTime.now,
        authLocation = "",
        latitude = None,
        longitude = None
      )
      _ ← fromDBIO(refreshAuthSession(transaction.deviceHash, authSession))
      _ ← fromDBIO(persist.auth.AuthTransactionRepo.delete(transaction.transactionHash))
      _ ← fromFuture(authorize(userId, clientData))
    } yield userStruct
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
    emailSender.send(Some(transactionHash), EmailCode(email, code))
  }

  protected def genSmsHash() = ThreadLocalRandom.current.nextLong().toString

  protected def genEmailCode(email: String): String =
    if (isTestEmail(email)) genTestCode(email) else genCode()

  protected def genSmsCode(phone: Long): String = phone.toString match {
    case strNumber if isTestPhone(phone) ⇒ Try(strNumber(4).toString * 4).getOrElse(phone.toString)
    case _                               ⇒ genCode()
  }

  protected def newUser(name: String, countryCode: String, optSex: Option[ApiSex], username: Option[String]): Result[\/-[User]] = {
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
        external = None,
        nickname = username
      )
    } yield \/-(user)
  }

  protected def newUser(username: Option[String]): Result[User] = {
    val rng = ThreadLocalRandom.current()
    val user = model.User(
      id = nextIntId(rng),
      accessSalt = ACLUtils.nextAccessSalt(rng),
      name = "",
      countryCode = "",
      sex = model.NoSex,
      state = model.UserState.Registered,
      createdAt = LocalDateTime.now(ZoneOffset.UTC),
      external = None,
      nickname = username
    )
    point(user)
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
