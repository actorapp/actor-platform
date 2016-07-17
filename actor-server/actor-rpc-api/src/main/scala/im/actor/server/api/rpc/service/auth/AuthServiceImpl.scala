package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.auth.ApiEmailActivationType._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.users.ApiSex.ApiSex
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.common.CodeFailure
import im.actor.server.activation.ActivationContext
import im.actor.server.api.rpc.service.profile.ProfileRpcErrors
import im.actor.server.auth.DeviceInfo
import im.actor.server.db.DbExtension
import im.actor.server.email.{ EmailConfig, SmtpEmailSender }
import im.actor.server.model._
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.oauth.GoogleProvider
import im.actor.server.persist._
import im.actor.server.persist.auth.{ AuthEmailTransactionRepo, AuthPhoneTransactionRepo, AuthTransactionRepo, AuthUsernameTransactionRepo }
import im.actor.server.session._
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user.{ UserErrors, UserExtension }
import im.actor.util.log.AnyRefLogSource
import im.actor.util.misc.PhoneNumberUtils._
import im.actor.util.misc._
import im.actor.util.ThreadLocalSecureRandom
import org.joda.time.DateTime
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

final class AuthServiceImpl(val oauth2Service: GoogleProvider)(
  implicit
  val actorSystem:   ActorSystem,
  val sessionRegion: SessionRegion
) extends AuthService
  with AuthHelpers
  with Helpers
  with DeprecatedAuthMethods {

  import AnyRefLogSource._
  import IdUtils._
  import DBIOResultRpc._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  protected implicit val db: Database = DbExtension(actorSystem).db
  protected val userExt = UserExtension(actorSystem)
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region
  protected val activationContext = new ActivationContext
  protected val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  private implicit val mat = ActorMaterializer()

  protected val log = Logging(actorSystem, this)

  private val maxGroupSize: Int = 300

  implicit protected val timeout = Timeout(10 seconds)

  override def doHandleGetAuthSessions(clientData: ClientData): Future[HandlerResult[ResponseGetAuthSessions]] =
    authorized(clientData) { client ⇒
      val action = for {
        sessionModels ← AuthSessionRepo.findByUserId(client.userId)
      } yield {
        val sessionStructs = sessionModels map { sessionModel ⇒
          val authHolder =
            if (client.authId == sessionModel.authId) {
              ApiAuthHolder.ThisDevice
            } else {
              ApiAuthHolder.OtherDevice
            }

          ApiAuthSession(
            sessionModel.id,
            authHolder,
            sessionModel.appId,
            sessionModel.appTitle,
            sessionModel.deviceTitle,
            (sessionModel.authTime.getMillis / 1000).toInt,
            sessionModel.authLocation,
            sessionModel.latitude,
            sessionModel.longitude
          )
        }

        Ok(ResponseGetAuthSessions(sessionStructs.toVector))
      }
      db.run(action)
    }

  override def doHandleCompleteOAuth2(transactionHash: String, code: String, clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        transaction ← fromDBIOOption(AuthErrors.EmailCodeExpired)(AuthEmailTransactionRepo.find(transactionHash))
        token ← fromDBIOOption(AuthErrors.EmailCodeExpired)(oauth2Service.completeOAuth(code, transaction.email, transaction.redirectUri))
        profile ← fromFutureOption(AuthErrors.EmailCodeExpired)(oauth2Service.fetchProfile(token.accessToken))

        _ ← fromBoolean(AuthErrors.OAuthUserIdDoesNotMatch)(transaction.email == profile.email)
        _ ← fromDBIO(OAuth2TokenRepo.createOrUpdate(token))

        _ ← fromDBIO(AuthTransactionRepo.updateSetChecked(transactionHash))

        email ← fromDBIOOption(AuthErrors.EmailUnoccupied)(UserEmailRepo.find(transaction.email))

        user ← authorizeT(email.userId, profile.locale.getOrElse(""), transaction, clientData)
        userStruct ← fromFuture(userExt.getApiStruct(user.id, user.id, clientData.authId))

        //refresh session data
        authSession = AuthSession(
          userId = user.id,
          id = nextIntId(),
          authId = clientData.authId,
          appId = transaction.appId,
          appTitle = AuthSession.appTitleOf(transaction.appId),
          deviceHash = transaction.deviceHash,
          deviceTitle = transaction.deviceTitle,
          authTime = DateTime.now,
          authLocation = "",
          latitude = None,
          longitude = None
        )
        //        _ ← fromDBIO(refreshAuthSession(transaction.deviceHash, authSession))
        //        _ ← fromDBIO(AuthTransactionRepo.delete(transactionHash))
        //        ack ← fromFuture(authorize(user.id, authSession.id, clientData))
      } yield ResponseAuth(userStruct, misc.ApiConfig(maxGroupSize))
    db.run(action.value)
  }

  override def doHandleGetOAuth2Params(transactionHash: String, redirectUrl: String, clientData: ClientData): Future[HandlerResult[ResponseGetOAuth2Params]] = {
    val action =
      for {
        transaction ← fromDBIOOption(AuthErrors.EmailCodeExpired)(AuthEmailTransactionRepo.find(transactionHash))
        url ← fromOption(AuthErrors.RedirectUrlInvalid)(oauth2Service.getAuthUrl(redirectUrl, transaction.email))
        _ ← fromDBIO(AuthEmailTransactionRepo.updateRedirectUri(transaction.transactionHash, redirectUrl))
      } yield ResponseGetOAuth2Params(url)
    db.run(action.value)
  }

  override def doHandleStartPhoneAuth(
    phoneNumber:        Long,
    appId:              Int,
    apiKey:             String,
    deviceHash:         Array[Byte],
    deviceTitle:        String,
    timeZone:           Option[String],
    preferredLanguages: IndexedSeq[String],
    clientData:         ClientData
  ): Future[HandlerResult[ResponseStartPhoneAuth]] = {
    val action = for {
      normalizedPhone ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeLong(phoneNumber).headOption)
      optPhone ← fromDBIO(UserPhoneRepo.findByPhoneNumber(normalizedPhone).headOption)
      _ ← optPhone map (p ⇒ forbidDeletedUser(p.userId)) getOrElse point(())
      optAuthTransaction ← fromDBIO(AuthPhoneTransactionRepo.findByPhoneAndDeviceHash(normalizedPhone, deviceHash))
      transactionHash ← optAuthTransaction match {
        case Some(transaction) ⇒ point(transaction.transactionHash)
        case None ⇒
          val accessSalt = ACLUtils.nextAccessSalt()
          val transactionHash = ACLUtils.authTransactionHash(accessSalt)
          val phoneAuthTransaction = AuthPhoneTransaction(
            normalizedPhone,
            transactionHash,
            appId,
            apiKey,
            deviceHash,
            deviceTitle,
            accessSalt,
            DeviceInfo(timeZone.getOrElse(""), preferredLanguages).toByteArray
          )
          for {
            _ ← fromDBIO(AuthPhoneTransactionRepo.create(phoneAuthTransaction))
          } yield transactionHash
      }
      _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendSmsCode(normalizedPhone, transactionHash))
      isRegistered = optPhone.isDefined
    } yield ResponseStartPhoneAuth(transactionHash, isRegistered, Some(ApiPhoneActivationType.CODE))
    db.run(action.value)
  }

  override def doHandleStartUsernameAuth(
    username:           String,
    appId:              Int,
    apiKey:             String,
    deviceHash:         Array[Byte],
    deviceTitle:        String,
    timeZone:           Option[String],
    preferredLanguages: IndexedSeq[String],
    clientData:         ClientData
  ): Future[HandlerResult[ResponseStartUsernameAuth]] = {
    val action =
      for {
        normUsername ← fromOption(ProfileRpcErrors.NicknameInvalid)(StringUtils.normalizeUsername(username))
        optUserId ← fromFuture(globalNamesStorage.getUserOwnerId(username))
        _ ← optUserId map (id ⇒ forbidDeletedUser(id)) getOrElse point(())
        optAuthTransaction ← fromDBIO(AuthUsernameTransactionRepo.find(username, deviceHash))
        transactionHash ← optAuthTransaction match {
          case Some(transaction) ⇒ point(transaction.transactionHash)
          case None ⇒
            val accessSalt = ACLUtils.nextAccessSalt()
            val transactionHash = ACLUtils.authTransactionHash(accessSalt)
            val authTransaction = AuthUsernameTransaction(
              normUsername,
              optUserId,
              transactionHash,
              appId,
              apiKey,
              deviceHash,
              deviceTitle,
              accessSalt,
              DeviceInfo(timeZone.getOrElse(""), preferredLanguages).toByteArray,
              isChecked = optUserId.isEmpty // we don't need to check password if user signs up
            )
            for (_ ← fromDBIO(AuthUsernameTransactionRepo.create(authTransaction))) yield transactionHash
        }
      } yield ResponseStartUsernameAuth(transactionHash, optUserId.isDefined)

    db.run(action.value)
  }

  override def doHandleStartAnonymousAuth(
    username:           String,
    appId:              Int,
    apiKey:             String,
    deviceHash:         Array[Byte],
    deviceTitle:        String,
    timeZone:           Option[String],
    preferredLanguages: IndexedSeq[String],
    clientData:         ClientData
  ): Future[HandlerResult[ResponseAuth]] = {
    val action =
      for {
        normUsername ← fromOption(ProfileRpcErrors.NicknameInvalid)(StringUtils.normalizeUsername(username))
        accessSalt = ACLUtils.nextAccessSalt()
        nicknameExists ← fromFuture(globalNamesStorage.exists(normUsername))
        _ ← fromBoolean(ProfileRpcErrors.NicknameBusy)(!nicknameExists)
        transactionHash = ACLUtils.authTransactionHash(accessSalt)
        transaction = AuthAnonymousTransaction(
          normUsername,
          transactionHash,
          appId,
          apiKey,
          deviceHash,
          deviceTitle,
          accessSalt,
          DeviceInfo(timeZone.getOrElse(""), preferredLanguages).toByteArray,
          isChecked = false // we don't need to check password if user signs up
        )
        user ← newUser(normUsername)
        _ ← handleUserCreate(user, transaction, clientData)
        userStruct ← authorizeT(user.id, "", transaction, clientData)
      } yield ResponseAuth(userStruct, ApiConfig(maxGroupSize))
    db.run(action.value)
  }

  override def doHandleSendCodeByPhoneCall(transactionHash: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = for {
      tx ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthPhoneTransactionRepo.find(transactionHash))
      lang = PhoneNumberUtils.normalizeWithCountry(tx.phoneNumber).headOption.map(_._2).getOrElse("en")
      _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendCallCode(tx.phoneNumber, transactionHash, lang))
    } yield ResponseVoid

    db.run(action.value)
  }

  override def doHandleSignUp(transactionHash: String, name: String, sex: Option[ApiSex], password: Option[String], clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        //retrieve `authTransaction`
        _ ← fromBoolean(AuthErrors.PasswordInvalid)(password map ACLUtils.isPasswordValid getOrElse true)
        transaction ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthTransactionRepo.findChildren(transactionHash))
        //ensure that `authTransaction` is checked
        _ ← fromBoolean(AuthErrors.NotValidated)(transaction.isChecked)
        signInORsignUp ← transaction match {
          case p: AuthPhoneTransaction     ⇒ newUserPhoneSignUp(p, name, sex)
          case e: AuthEmailTransaction     ⇒ newUserEmailSignUp(e, name, sex)
          case u: AuthUsernameTransaction  ⇒ newUsernameSignUp(u, name, sex)
          case _: AuthAnonymousTransaction ⇒ fromEither(Xor.left(AuthErrors.NotValidated))
        }
        //fallback to sign up if user exists
        userStruct ← signInORsignUp match {
          case Xor.Left((userId, countryCode)) ⇒ authorizeT(userId, countryCode, transaction, clientData)
          case Xor.Right(user) ⇒
            for {
              _ ← handleUserCreate(user, transaction, clientData)
              userStruct ← authorizeT(user.id, "", transaction, clientData)
            } yield userStruct
        }
        _ ← fromDBIO(password match {
          case Some(p) ⇒
            val (hash, salt) = ACLUtils.hashPassword(p)
            UserPasswordRepo.createOrReplace(userStruct.id, hash, salt)
          case None ⇒ DBIO.successful(0)
        })
      } yield ResponseAuth(userStruct, misc.ApiConfig(maxGroupSize))
    db.run(action.value)
  }

  override def doHandleStartEmailAuth(
    email:              String,
    appId:              Int,
    apiKey:             String,
    deviceHash:         Array[Byte],
    deviceTitle:        String,
    timeZone:           Option[String],
    preferredLanguages: IndexedSeq[String],
    clientData:         ClientData
  ): Future[HandlerResult[ResponseStartEmailAuth]] = {
    val action = for {
      validEmail ← fromEither(validEmail(email).leftMap(validationFailed("EMAIL_INVALID", _)))
      optEmail ← fromDBIO(UserEmailRepo.find(validEmail))
      _ ← optEmail map (e ⇒ forbidDeletedUser(e.userId)) getOrElse point(())
      //    OAUTH activation is temporary disabled
      //    activationType = if (OAuth2ProvidersDomains.supportsOAuth2(validEmail)) OAUTH2 else CODE
      activationType = CODE
      isRegistered = optEmail.isDefined
      optTransaction ← fromDBIO(AuthEmailTransactionRepo.findByEmailAndDeviceHash(validEmail, deviceHash))
      transactionHash ← optTransaction match {
        case Some(trans) ⇒
          val hash = trans.transactionHash
          activationType match {
            case CODE ⇒
              for {
                _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendEmailCode(validEmail, hash))
              } yield hash
            case OAUTH2 ⇒
              point(hash)
          }
        case None ⇒
          val accessSalt = ACLUtils.nextAccessSalt()
          val transactionHash = ACLUtils.authTransactionHash(accessSalt)
          val emailAuthTransaction = AuthEmailTransaction(
            validEmail,
            None,
            transactionHash,
            appId,
            apiKey,
            deviceHash,
            deviceTitle,
            accessSalt,
            DeviceInfo(timeZone.getOrElse(""), preferredLanguages).toByteArray
          )
          activationType match {
            case CODE ⇒
              for {
                _ ← fromDBIO(AuthEmailTransactionRepo.create(emailAuthTransaction))
                _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendEmailCode(validEmail, transactionHash))
              } yield transactionHash
            case OAUTH2 ⇒
              for {
                _ ← fromDBIO(AuthEmailTransactionRepo.create(emailAuthTransaction))
              } yield transactionHash
          }
      }
    } yield ResponseStartEmailAuth(transactionHash, isRegistered, activationType)
    db.run(action.value)
  }

  override def doHandleValidateCode(transactionHash: String, code: String, clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        //retreive `authTransaction`
        transaction ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthTransactionRepo.findChildren(transactionHash))

        //validate code
        validate ← validateCode(transaction, code)
        (userId, countryCode) = validate

        //sign in user and delete auth transaction
        userStruct ← authorizeT(userId, countryCode, transaction, clientData)
      } yield ResponseAuth(userStruct, misc.ApiConfig(maxGroupSize))
    db.run(action.value)
  }

  override def doHandleValidatePassword(
    transactionHash: String,
    password:        String,
    clientData:      ClientData
  ): Future[HandlerResult[ResponseAuth]] = {
    val action =
      for {
        transaction ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthUsernameTransactionRepo.find(transactionHash))
        validate ← validateCode(transaction, password)
        (userId, countryCode) = validate
        userStruct ← authorizeT(userId, countryCode, transaction, clientData)
      } yield ResponseAuth(userStruct, ApiConfig(maxGroupSize))

    db.run(action.value)
  }

  override def doHandleSignOut(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { implicit client ⇒
      val action = AuthSessionRepo.findByAuthId(client.authId) flatMap {
        case Some(session) ⇒
          for (_ ← DBIO.from(userExt.logout(session))) yield Ok(misc.ResponseVoid)
        case None ⇒ throw new Exception(s"Cannot find AuthSession for authId: ${client.authId}")
      }
      db.run(action)
    }

  override def doHandleTerminateAllSessions(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      val action = for {
        sessions ← AuthSessionRepo.findByUserId(client.userId) map (_.filterNot(_.authId == client.authId))
        _ ← DBIO.from(Future.sequence(sessions map userExt.logout))
      } yield {
        Ok(ResponseVoid)
      }
      db.run(action)
    }

  override def doHandleTerminateSession(id: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      val action = AuthSessionRepo.find(client.userId, id).headOption flatMap {
        case Some(session) ⇒
          if (session.authId != clientData.authId) {
            for (_ ← DBIO.from(userExt.logout(session))) yield Ok(ResponseVoid)
          } else {
            DBIO.successful(Error(AuthErrors.CurrentSessionTermination))
          }
        case None ⇒
          DBIO.successful(Error(AuthErrors.AuthSessionNotFound))
      }
      db.run(action)
    }

  override def doHandleStartTokenAuth(token: String, appId: Int, apiKey: String, deviceHash: Array[Byte], deviceTitle: String, timeZone: Option[String], preferredLanguages: IndexedSeq[String], clientData: ClientData): Future[HandlerResult[ResponseAuth]] =
    Future.failed(new RuntimeException("Not implemented"))

  override def onFailure: PartialFunction[Throwable, RpcError] = recoverCommon orElse {
    case UserErrors.NicknameTaken ⇒ ProfileRpcErrors.NicknameBusy
  }

}
