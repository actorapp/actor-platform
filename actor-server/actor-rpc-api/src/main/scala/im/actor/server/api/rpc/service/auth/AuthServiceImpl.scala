package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.auth.ApiEmailActivationType._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.users.ApiSex.ApiSex
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.internal.{ DummyCallEngine, DummySmsEngine, ActivationConfig, InternalCodeActivation }
import im.actor.server.activation.{ CodeFailure, CodeActivation }
import im.actor.server.api.rpc.service.profile.ProfileErrors
import im.actor.server.auth.DeviceInfo
import im.actor.server.db.DbExtension
import im.actor.server.email.{ EmailConfig, SmtpEmailSender }
import im.actor.server.model._
import im.actor.server.oauth.GoogleProvider
import im.actor.server.persist._
import im.actor.server.persist.auth.{ AuthUsernameTransactionRepo, AuthPhoneTransactionRepo, AuthTransactionRepo, AuthEmailTransactionRepo }
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
import scalaz._

final class AuthServiceImpl(val activationContext: CodeActivation)(
  implicit
  val sessionRegion: SessionRegion,
  val actorSystem:   ActorSystem,
  val oauth2Service: GoogleProvider
) extends AuthService with AuthHelpers with Helpers {

  import AnyRefLogSource._
  import IdUtils._

  private trait SignType
  private case class Up(name: String, isSilent: Boolean) extends SignType
  private case object In extends SignType

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  protected implicit val db: Database = DbExtension(actorSystem).db
  protected val userExt = UserExtension(actorSystem)
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region
  private implicit val mat = ActorMaterializer()

  // this is workaround to use internal mail sender, when using actor-activation option
  protected val emailSender = ActorConfig.load().getString("services.activation.default-service") match {
    case "internal" | "telesign" ⇒ activationContext
    case "actor-activation" ⇒
      InternalCodeActivation.newContext(
        ActivationConfig.load.get,
        new DummySmsEngine(),
        new DummyCallEngine(),
        new SmtpEmailSender(EmailConfig.load.get)
      )
  }

  protected val log = Logging(actorSystem, this)

  private val maxGroupSize: Int = 300

  implicit protected val timeout = Timeout(10 seconds)

  override def jhandleGetAuthSessions(clientData: ClientData): Future[HandlerResult[ResponseGetAuthSessions]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      for {
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
    }

    db.run(toDBIOAction(authorizedAction))
  }

  def jhandleCompleteOAuth2(transactionHash: String, code: String, clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
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
        _ ← fromDBIO(refreshAuthSession(transaction.deviceHash, authSession))
        _ ← fromDBIO(AuthTransactionRepo.delete(transactionHash))
        ack ← fromFuture(authorize(user.id, authSession.id, clientData))
      } yield ResponseAuth(userStruct, misc.ApiConfig(maxGroupSize))
    db.run(action.run)
  }

  def jhandleGetOAuth2Params(transactionHash: String, redirectUrl: String, clientData: ClientData): Future[HandlerResult[ResponseGetOAuth2Params]] = {
    val action =
      for {
        transaction ← fromDBIOOption(AuthErrors.EmailCodeExpired)(AuthEmailTransactionRepo.find(transactionHash))
        url ← fromOption(AuthErrors.RedirectUrlInvalid)(oauth2Service.getAuthUrl(redirectUrl, transaction.email))
        _ ← fromDBIO(AuthEmailTransactionRepo.updateRedirectUri(transaction.transactionHash, redirectUrl))
      } yield ResponseGetOAuth2Params(url)
    db.run(action.run)
  }

  def jhandleStartPhoneAuth(
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
      _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendSmsCode(normalizedPhone, genSmsCode(normalizedPhone), Some(transactionHash)))
      isRegistered = optPhone.isDefined
    } yield ResponseStartPhoneAuth(transactionHash, isRegistered, Some(ApiPhoneActivationType.CODE))
    db.run(action.run)
  }

  override def jhandleStartUsernameAuth(
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
        normUsername ← fromOption(ProfileErrors.NicknameInvalid)(StringUtils.normalizeUsername(username))
        optUser ← fromDBIO(UserRepo.findByNickname(username))
        _ ← optUser map (u ⇒ forbidDeletedUser(u.id)) getOrElse point(())
        optAuthTransaction ← fromDBIO(AuthUsernameTransactionRepo.find(username, deviceHash))
        transactionHash ← optAuthTransaction match {
          case Some(transaction) ⇒ point(transaction.transactionHash)
          case None ⇒
            val accessSalt = ACLUtils.nextAccessSalt()
            val transactionHash = ACLUtils.authTransactionHash(accessSalt)
            val authTransaction = AuthUsernameTransaction(
              normUsername,
              optUser map (_.id),
              transactionHash,
              appId,
              apiKey,
              deviceHash,
              deviceTitle,
              accessSalt,
              DeviceInfo(timeZone.getOrElse(""), preferredLanguages).toByteArray,
              isChecked = optUser.isEmpty // we don't need to check password if user signs up
            )
            for (_ ← fromDBIO(AuthUsernameTransactionRepo.create(authTransaction))) yield transactionHash
        }
      } yield ResponseStartUsernameAuth(transactionHash, optUser.isDefined)

    db.run(action.run)
  }

  override def jhandleStartAnonymousAuth(
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
        normUsername ← fromOption(ProfileErrors.NicknameInvalid)(StringUtils.normalizeUsername(username))
        accessSalt = ACLUtils.nextAccessSalt()
        nicknameExists ← fromDBIO(UserRepo.nicknameExists(normUsername))
        _ ← fromBoolean(ProfileErrors.NicknameBusy)(!nicknameExists)
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

    recover(db.run(action.run)) recover {
      case UserErrors.NicknameTaken ⇒ Error(ProfileErrors.NicknameBusy)
    }
  }

  override def jhandleSendCodeByPhoneCall(transactionHash: String, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = for {
      tx ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthPhoneTransactionRepo.find(transactionHash))
      code ← fromDBIO(AuthCodeRepo.findByTransactionHash(tx.transactionHash) map (_ map (_.code) getOrElse (genSmsCode(tx.phoneNumber))))
      lang = PhoneNumberUtils.normalizeWithCountry(tx.phoneNumber).headOption.map(_._2).getOrElse("en")
      _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendCallCode(tx.phoneNumber, genSmsCode(tx.phoneNumber), Some(transactionHash), lang))
    } yield ResponseVoid

    db.run(action.run)
  }

  def jhandleSignUp(transactionHash: String, name: String, sex: Option[ApiSex], password: Option[String], clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
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
          case _: AuthAnonymousTransaction ⇒ fromEither(-\/(AuthErrors.NotValidated))
        }
        //fallback to sign up if user exists
        userStruct ← signInORsignUp match {
          case -\/((userId, countryCode)) ⇒ authorizeT(userId, countryCode, transaction, clientData)
          case \/-(user) ⇒
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
    db.run(action.run)
  }

  override def jhandleStartEmailAuth(
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
                _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendEmailCode(validEmail, genEmailCode(validEmail), hash))
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
                _ ← fromDBIOEither[Unit, CodeFailure](AuthErrors.activationFailure)(sendEmailCode(validEmail, genEmailCode(validEmail), transactionHash))
              } yield transactionHash
            case OAUTH2 ⇒
              for {
                _ ← fromDBIO(AuthEmailTransactionRepo.create(emailAuthTransaction))
              } yield transactionHash
          }
      }
    } yield ResponseStartEmailAuth(transactionHash, isRegistered, activationType)
    db.run(action.run)
  }

  //TODO: add email code validation
  def jhandleValidateCode(transactionHash: String, code: String, clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        //retreive `authTransaction`
        transaction ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthTransactionRepo.findChildren(transactionHash))

        //validate code
        (userId, countryCode) ← validateCode(transaction, code)

        //sign in user and delete auth transaction
        userStruct ← authorizeT(userId, countryCode, transaction, clientData)
      } yield ResponseAuth(userStruct, misc.ApiConfig(maxGroupSize))
    db.run(action.run)
  }

  override def jhandleValidatePassword(
    transactionHash: String,
    password:        String,
    clientData:      ClientData
  ): Future[HandlerResult[ResponseAuth]] = {
    val action =
      for {
        transaction ← fromDBIOOption(AuthErrors.PhoneCodeExpired)(AuthUsernameTransactionRepo.find(transactionHash))
        (userId, countryCode) ← validateCode(transaction, password)
        userStruct ← authorizeT(userId, countryCode, transaction, clientData)
      } yield ResponseAuth(userStruct, ApiConfig(maxGroupSize))

    db.run(action.run)
  }

  override def jhandleSignOut(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      AuthSessionRepo.findByAuthId(client.authId) flatMap {
        case Some(session) ⇒
          for (_ ← DBIO.from(userExt.logout(session))) yield Ok(misc.ResponseVoid)
        case None ⇒ throw new Exception(s"Cannot find AuthSession for authId: ${client.authId}")
      }
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleTerminateAllSessions(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      for {
        sessions ← AuthSessionRepo.findByUserId(client.userId) map (_.filterNot(_.authId == client.authId))
        _ ← DBIO.from(Future.sequence(sessions map userExt.logout))
      } yield {
        Ok(ResponseVoid)
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleTerminateSession(id: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      AuthSessionRepo.find(client.userId, id).headOption flatMap {
        case Some(session) ⇒
          if (session.authId != clientData.authId) {
            for (_ ← DBIO.from(userExt.logout(session))) yield Ok(ResponseVoid)
          } else {
            DBIO.successful(Error(AuthErrors.CurrentSessionTermination))
          }
        case None ⇒
          DBIO.successful(Error(AuthErrors.AuthSessionNotFound))
      }

    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleStartTokenAuth(token: String, appId: Int, apiKey: String, deviceHash: Array[Byte], deviceTitle: String, timeZone: Option[String], preferredLanguages: IndexedSeq[String], clientData: ClientData): Future[HandlerResult[ResponseAuth]] =
    Future.failed(new RuntimeException("Not implemented"))

  //TODO: move deprecated methods to separate trait
  @deprecated("schema api changes", "2015-06-09")
  override def jhandleSendAuthCallObsolete(
    phoneNumber: Long,
    smsHash:     String,
    appId:       Int,
    apiKey:      String,
    clientData:  ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    Future {
      throw new Exception("Not implemented")
    }

  @deprecated("schema api changes", "2015-06-09")
  override def jhandleSendAuthCodeObsolete(
    rawPhoneNumber: Long,
    appId:          Int,
    apiKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseSendAuthCodeObsolete]] = {
    PhoneNumberUtils.normalizeLong(rawPhoneNumber).headOption match {
      case None ⇒
        Future.successful(Error(AuthErrors.PhoneNumberInvalid))
      case Some(normPhoneNumber) ⇒
        val isDeletedAction = for {
          optPhone ← UserPhoneRepo.findByPhoneNumber(normPhoneNumber).headOption
          isDeleted ← optPhone match {
            case Some(phone) ⇒ UserRepo.isDeleted(phone.userId)
            case None        ⇒ DBIO.successful(false)
          }
        } yield isDeleted

        val sendCodeAction = AuthSmsCodeObsoleteRepo.findByPhoneNumber(normPhoneNumber).headOption.flatMap {
          case Some(AuthSmsCodeObsolete(_, _, smsHash, smsCode, _)) ⇒
            DBIO.successful(normPhoneNumber :: smsHash :: smsCode :: HNil)
          case None ⇒
            val smsHash = genSmsHash()
            val smsCode = genSmsCode(normPhoneNumber)
            for (
              _ ← AuthSmsCodeObsoleteRepo.create(
                id = ThreadLocalSecureRandom.current().nextLong(),
                phoneNumber = normPhoneNumber,
                smsHash = smsHash,
                smsCode = smsCode
              )
            ) yield normPhoneNumber :: smsHash :: smsCode :: HNil
        }.flatMap { res ⇒
          UserPhoneRepo.exists(normPhoneNumber) map (res :+ _)
        }.map {
          case number :: smsHash :: smsCode :: isRegistered :: HNil ⇒
            sendSmsCode(number, smsCode, None)
            Ok(ResponseSendAuthCodeObsolete(smsHash, isRegistered))
        }
        db.run(for {
          isDeleted ← isDeletedAction
          result ← if (isDeleted) DBIO.successful(Error(AuthErrors.UserDeleted)) else sendCodeAction
        } yield result)
    }
  }

  @deprecated("schema api changes", "2015-06-09")
  override def jhandleSignInObsolete(
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(
      In,
      rawPhoneNumber, smsHash, smsCode,
      deviceHash, deviceTitle, appId, appKey,
      clientData
    )

  @deprecated("schema api changes", "2015-06-09")
  override def jhandleSignUpObsolete(
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    name:           String,
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    isSilent:       Boolean,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(
      Up(name, isSilent),
      rawPhoneNumber, smsHash, smsCode,
      deviceHash, deviceTitle, appId, appKey,
      clientData
    )

  private def handleSign(
    signType:       SignType,
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseAuth]] = {
    normalizeWithCountry(rawPhoneNumber).headOption match {
      case None ⇒ Future.successful(Error(AuthErrors.PhoneNumberInvalid))
      case Some((normPhoneNumber, countryCode)) ⇒
        if (smsCode.isEmpty) Future.successful(Error(AuthErrors.PhoneCodeEmpty))
        else {
          val action =
            (for {
              optCode ← AuthSmsCodeObsoleteRepo.findByPhoneNumber(normPhoneNumber).headOption
              optPhone ← UserPhoneRepo.findByPhoneNumber(normPhoneNumber).headOption
            } yield optCode :: optPhone :: HNil).flatMap {
              case None :: _ :: HNil ⇒ DBIO.successful(Error(AuthErrors.PhoneCodeExpired))
              case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsHash != smsHash ⇒
                DBIO.successful(Error(AuthErrors.PhoneCodeExpired))
              case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsCode != smsCode ⇒
                DBIO.successful(Error(AuthErrors.PhoneCodeInvalid))
              case Some(_) :: optPhone :: HNil ⇒
                signType match {
                  case Up(rawName, isSilent) ⇒
                    AuthSmsCodeObsoleteRepo.deleteByPhoneNumber(normPhoneNumber).andThen(
                      optPhone match {
                        // Phone does not exist, register the user
                        case None ⇒ withValidName(rawName) { name ⇒
                          val rng = ThreadLocalSecureRandom.current()
                          val userId = nextIntId(rng)
                          //todo: move this to UserOffice
                          val user = User(
                            id = userId,
                            accessSalt = ACLUtils.nextAccessSalt(rng),
                            name = name,
                            countryCode = countryCode,
                            sex = NoSex,
                            state = UserState.Registered,
                            createdAt = LocalDateTime.now(ZoneOffset.UTC),
                            external = None
                          )
                          for {
                            _ ← DBIO.from(userExt.create(user.id, user.accessSalt, None, user.name, user.countryCode, im.actor.api.rpc.users.ApiSex(user.sex.toInt), isBot = false))
                            _ ← DBIO.from(userExt.auth(userId, clientData.authId))
                            _ ← DBIO.from(userExt.addPhone(user.id, normPhoneNumber))
                            _ ← AvatarDataRepo.create(AvatarData.empty(AvatarData.OfUser, user.id.toLong))
                          } yield {
                            \/-(user :: HNil)
                          }
                        }
                        // Phone already exists, fall back to SignIn
                        case Some(phone) ⇒
                          signIn(clientData.authId, phone.userId, countryCode, clientData)
                      }
                    )
                  case In ⇒
                    optPhone match {
                      case None ⇒ DBIO.successful(Error(AuthErrors.PhoneNumberUnoccupied))
                      case Some(phone) ⇒
                        AuthSmsCodeObsoleteRepo.deleteByPhoneNumber(normPhoneNumber).andThen(
                          signIn(clientData.authId, phone.userId, countryCode, clientData)
                        )
                    }
                }
            }.flatMap {
              case \/-(user :: HNil) ⇒
                val authSession = AuthSession(
                  userId = user.id,
                  id = nextIntId(),
                  authId = clientData.authId,
                  appId = appId,
                  appTitle = AuthSession.appTitleOf(appId),
                  deviceHash = deviceHash,
                  deviceTitle = deviceTitle,
                  authTime = DateTime.now,
                  authLocation = "",
                  latitude = None,
                  longitude = None
                )

                for {
                  prevSessions ← AuthSessionRepo.findByDeviceHash(deviceHash)
                  _ ← DBIO.from(Future.sequence(prevSessions map userExt.logout))
                  _ ← AuthSessionRepo.create(authSession)
                  userStruct ← DBIO.from(userExt.getApiStruct(user.id, user.id, clientData.authId))
                } yield {
                  sessionRegion.ref ! SessionEnvelope(clientData.authId, clientData.sessionId).withAuthorizeUser(AuthorizeUser(userStruct.id, authSession.id))
                  Ok(
                    ResponseAuth(
                      userStruct,
                      misc.ApiConfig(maxGroupSize)
                    )
                  )

                }
              case error @ -\/(_) ⇒ DBIO.successful(error)
            }

          db.run(action)
        }
    }
  }

  private def signIn(authId: Long, userId: Int, countryCode: String, clientData: ClientData) = {
    UserRepo.find(userId).flatMap {
      case None ⇒ throw new Exception("Failed to retrieve user")
      case Some(user) ⇒
        for {
          _ ← DBIO.from(userExt.changeCountryCode(userId, countryCode))
          _ ← DBIO.from(userExt.auth(userId, clientData.authId))
        } yield \/-(user :: HNil)
    }
  }

}
