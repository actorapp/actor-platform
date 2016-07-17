package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.users.ApiSex._
import im.actor.api.rpc.users.ApiUser
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.common._
import im.actor.server.auth.DeviceInfo
import im.actor.server.model._
import im.actor.server.persist._
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.session._
import im.actor.util.misc.IdUtils._
import im.actor.util.misc.PhoneNumberUtils._
import im.actor.util.misc.StringUtils.validName
import im.actor.util.ThreadLocalSecureRandom
import org.joda.time.DateTime
import slick.dbio._

import scala.concurrent.Future

trait AuthHelpers extends Helpers {
  self: AuthServiceImpl ⇒

  import DBIOResultRpc._

  protected def newUserPhoneSignUp(transaction: AuthPhoneTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) Xor User] = {
    val phone = transaction.phoneNumber
    for {
      optPhone ← fromDBIO(UserPhoneRepo.findByPhoneNumber(phone).headOption)
      phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
      (_, countryCode) = phoneAndCode
      result ← optPhone match {
        case Some(userPhone) ⇒ point(Xor.left((userPhone.userId, countryCode)))
        case None            ⇒ newUser(name, countryCode, sex, username = None)
      }
    } yield result
  }

  protected def newUserEmailSignUp(transaction: AuthEmailTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) Xor User] = {
    val email = transaction.email
    for {
      optEmail ← fromDBIO(UserEmailRepo.find(email))
      result ← optEmail match {
        case Some(existingEmail) ⇒ point(Xor.left((existingEmail.userId, "")))
        case None ⇒
          val userResult: Result[(Int, String) Xor User] =
            for {
              optToken ← fromDBIO(OAuth2TokenRepo.findByUserId(email))
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

  protected def newUsernameSignUp(transaction: AuthUsernameTransaction, name: String, sex: Option[ApiSex]): Result[(Int, String) Xor User] = {
    val username = transaction.username
    for {
      optUserId ← fromFuture(globalNamesStorage.getUserOwnerId(username))
      result ← optUserId match {
        case Some(id) ⇒ point(Xor.left((id, "")))
        case None     ⇒ newUser(name, "", sex, username = Some(username))
      }
    } yield result
  }

  protected def handleUserCreate(user: User, transaction: AuthTransactionBase, client: ClientData): Result[Unit] = {
    for {
      _ ← fromFuture(userExt.create(user.id, user.accessSalt, user.nickname, user.name, user.countryCode, im.actor.api.rpc.users.ApiSex(user.sex.toInt), isBot = false))
      _ ← fromDBIO(AvatarDataRepo.create(AvatarData.empty(AvatarData.OfUser, user.id.toLong)))
      _ ← transaction match {
        case p: AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            _ ← fromFuture(activationContext.cleanup(p))
            _ ← fromFuture(userExt.addPhone(user.id, phone))
          } yield ()
        case e: AuthEmailTransaction ⇒
          for {
            _ ← fromFuture(activationContext.cleanup(e))
            _ ← fromFuture(userExt.addEmail(user.id, e.email))
          } yield ()
        case u: AuthUsernameTransaction ⇒
          fromFuture(userExt.changeNickname(user.id, client.authId, Some(u.username)))
        case u: AuthAnonymousTransaction ⇒
          fromFuture(userExt.changeNickname(user.id, client.authId, Some(u.username)))
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
  protected def validateCode(transaction: AuthTransactionBase, code: String): Result[(Int, String)] = {
    val transactionHash = transaction.transactionHash
    for {
      _ ← transaction match {
        case tx: AuthTransactionBase with ExpirableCode ⇒
          val (codeExpired, codeInvalid) = expirationErrors(tx)
          for {
            validationResponse ← fromFuture(activationContext.validate(tx, code))
            _ ← validationResponse match {
              case ExpiredCode                     ⇒ cleanupAndError(transactionHash, codeExpired)
              case InvalidHash                     ⇒ cleanupAndError(transactionHash, AuthErrors.InvalidAuthCodeHash)
              case InvalidCode                     ⇒ fromEither[Unit](Xor.left(codeInvalid))
              case InvalidResponse | InternalError ⇒ cleanupAndError(transactionHash, AuthErrors.ActivationServiceError)
              case Validated                       ⇒ point(())
            }
            _ ← fromDBIO(AuthTransactionRepo.updateSetChecked(transactionHash))
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
        case tx: AuthAnonymousTransaction ⇒ fromEither(Xor.left(AuthErrors.NotValidated))
      }

      userAndCountry ← transaction match {
        case p: AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            //if user is not registered - return error
            phoneModel ← fromDBIOOption(AuthErrors.PhoneNumberUnoccupied)(UserPhoneRepo.findByPhoneNumber(phone).headOption)
            phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone).headOption)
            _ ← fromFuture(activationContext.cleanup(p))
          } yield (phoneModel.userId, phoneAndCode._2)
        case e: AuthEmailTransaction ⇒
          for {
            //if user is not registered - return error
            emailModel ← fromDBIOOption(AuthErrors.EmailUnoccupied)(UserEmailRepo.find(e.email))
            _ ← fromFuture(activationContext.cleanup(e))
          } yield (emailModel.userId, "")
        case u: AuthUsernameTransaction ⇒
          for {
            userId ← fromFutureOption(AuthErrors.UsernameUnoccupied)(globalNamesStorage.getUserOwnerId(u.username))
          } yield (userId, "")
        case _: AuthAnonymousTransaction ⇒
          fromEither(Xor.left(AuthErrors.NotValidated))
      }
    } yield userAndCountry
  }

  /**
   * Terminate all sessions associated with given `deviceHash`
   * and create new session
   */
  protected def refreshAuthSession(deviceHash: Array[Byte], newSession: AuthSession): DBIO[Unit] =
    for {
      // prevSessions ← persist.AuthSessionRepo.findByDeviceHash(deviceHash)
      //_ ← DBIO.from(Future.sequence(prevSessions map userExt.logout))
      _ ← AuthSessionRepo.create(newSession)
    } yield ()

  protected def authorize(userId: Int, authSid: Int, clientData: ClientData)(implicit sessionRegion: SessionRegion): Future[Unit] =
    for {
      _ ← userExt.auth(userId, clientData.authId)
      _ ← sessionRegion.ref
        .ask(SessionEnvelope(clientData.authId, clientData.sessionId).withAuthorizeUser(AuthorizeUser(userId, authSid)))
        .mapTo[AuthorizeUserAck]
    } yield ()

  //TODO: what country to use in case of email auth
  protected def authorizeT(
    userId:      Int,
    countryCode: String,
    transaction: AuthTransactionBase,
    client:      ClientData
  ): Result[ApiUser] = {
    for {
      _ ← fromFuture(if (countryCode.nonEmpty) userExt.changeCountryCode(userId, countryCode) else FastFuture.successful(()))
      _ ← fromFuture(userExt.setDeviceInfo(userId, client.authId, DeviceInfo.parseFrom(transaction.deviceInfo)) recover { case _ ⇒ () })
      _ ← fromDBIO(AuthIdRepo.setUserData(client.authId, userId))
      userStruct ← fromFuture(userExt.getApiStruct(userId, userId, client.authId))
      //refresh session data
      authSession = AuthSession(
        userId = userId,
        id = nextIntId(),
        authId = client.authId,
        appId = transaction.appId,
        appTitle = AuthSession.appTitleOf(transaction.appId),
        deviceHash = transaction.deviceHash,
        deviceTitle = transaction.deviceTitle,
        authTime = DateTime.now,
        authLocation = "",
        latitude = None,
        longitude = None
      )
      _ ← fromDBIO(refreshAuthSession(transaction.deviceHash, authSession))
      _ ← fromDBIO(AuthTransactionRepo.delete(transaction.transactionHash))
      _ ← fromFuture(authorize(userId, authSession.id, client))
    } yield userStruct
  }

  protected def sendSmsCode(phoneNumber: Long, txHash: String)(implicit system: ActorSystem): DBIO[CodeFailure Xor Unit] = {
    log.info("Sending sms code to {}", phoneNumber)
    DBIO.from(activationContext.send(txHash, SmsCode(phoneNumber)))
  }

  protected def sendCallCode(phoneNumber: Long, txHash: String, language: String)(implicit system: ActorSystem): DBIO[CodeFailure Xor Unit] = {
    log.info("Sending call code to {}", phoneNumber)
    DBIO.from(activationContext.send(txHash, CallCode(phoneNumber, language)))
  }

  protected def sendEmailCode(email: String, txHash: String)(implicit system: ActorSystem): DBIO[CodeFailure Xor Unit] = {
    log.info("Sending email code to {}", email)
    DBIO.from(activationContext.send(txHash, EmailCode(email)))
  }

  protected def newUser(name: String, countryCode: String, optSex: Option[ApiSex], username: Option[String]): Result[Xor.Right[User]] = {
    val rng = ThreadLocalSecureRandom.current()
    val sex = optSex.map(s ⇒ Sex.fromInt(s.id)).getOrElse(NoSex)
    for {
      validName ← fromEither(validName(name).leftMap(validationFailed("NAME_INVALID", _)))
      user = User(
        id = nextIntId(rng),
        accessSalt = ACLUtils.nextAccessSalt(rng),
        name = validName,
        countryCode = countryCode,
        sex = sex,
        state = UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC),
        external = None,
        nickname = username
      )
    } yield Xor.Right(user)
  }

  protected def newUser(name: String): Result[User] = {
    val rng = ThreadLocalSecureRandom.current()
    val user = User(
      id = nextIntId(rng),
      accessSalt = ACLUtils.nextAccessSalt(rng),
      name = name,
      countryCode = "",
      sex = NoSex,
      state = UserState.Registered,
      createdAt = LocalDateTime.now(ZoneOffset.UTC),
      external = None,
      nickname = None
    )
    point(user)
  }

  protected def forbidDeletedUser(userId: Int): Result[Unit] =
    fromDBIOBoolean(AuthErrors.UserDeleted)(UserRepo.isDeleted(userId).map(!_))

  private def cleanupAndError(transactionHash: String, error: RpcError): Result[Unit] = {
    for {
      _ ← fromDBIO(AuthTransactionRepo.delete(transactionHash))
      _ ← fromEither[Unit](Error(error))
    } yield ()
  }

}
