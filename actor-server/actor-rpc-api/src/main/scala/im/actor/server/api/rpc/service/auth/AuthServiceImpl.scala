package im.actor.server.api.rpc.service.auth

import java.time.{ ZoneOffset, LocalDateTime }

import scala.concurrent._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scalaz._
import scalaz.syntax.std.boolean._

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import org.joda.time.DateTime
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.auth.EmailActivationType._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.users.Sex.Sex
import im.actor.server.activation.ActivationContext
import im.actor.server.oauth.{ OAuth2ProvidersDomains, GoogleProvider }
import im.actor.server.persist.auth.AuthTransaction
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.session._
import im.actor.server.social.SocialManagerRegion
import im.actor.server.util.PhoneNumber._
import im.actor.server.util.UserUtils.userStruct
import im.actor.server.util._
import im.actor.server.{ persist, models }

sealed trait AuthEvent

object AuthEvents {
  case object AuthIdInvalidated extends AuthEvent
}

object AuthService {

  import akka.contrib.pattern.DistributedPubSubMediator._

  import AuthEvents._

  def authIdTopic(authId: Long): String = s"auth.events.${authId}"

  private[auth] def publishAuthIdInvalidated(mediator: ActorRef, authId: Long): Unit = {
    mediator ! Publish(authIdTopic(authId), AuthIdInvalidated)
  }
}

case class PubSubMediator(mediator: ActorRef)

class AuthServiceImpl(val activationContext: ActivationContext, mediator: ActorRef, val authConfig: AuthConfig)(
  implicit
  val sessionRegion:           SessionRegion,
  val seqUpdatesManagerRegion: SeqUpdatesManagerRegion,
  val socialManagerRegion:     SocialManagerRegion,
  val actorSystem:             ActorSystem,
  val db:                      Database,
  val oauth2Service:           GoogleProvider
) extends AuthService with AuthHelpers with Helpers {

  import AnyRefLogSource._
  import IdUtils._

  private trait SignType
  private case class Up(name: String, isSilent: Boolean) extends SignType
  private case object In extends SignType

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  protected val log = Logging(actorSystem, this)

  private val maxGroupSize: Int = 300

  implicit val mediatorWrap = PubSubMediator(mediator)

  override def jhandleGetAuthSessions(clientData: ClientData): Future[HandlerResult[ResponseGetAuthSessions]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      for {
        sessionModels ← persist.AuthSession.findByUserId(client.userId)
      } yield {
        val sessionStructs = sessionModels map { sessionModel ⇒
          val authHolder =
            if (client.authId == sessionModel.authId) {
              AuthHolder.ThisDevice
            } else {
              AuthHolder.OtherDevice
            }

          AuthSession(
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
        transaction ← fromDBIOOption(AuthErrors.InvalidAuthTransaction)(persist.auth.AuthEmailTransaction.find(transactionHash))
        token ← fromDBIOOption(AuthErrors.FailedToGetOAuth2Token)(oauth2Service.completeOAuth(code, transaction.email, transaction.redirectUri))
        profile ← fromFutureOption(AuthErrors.FailedToGetOAuth2Token)(oauth2Service.fetchProfile(token.accessToken))

        _ ← fromBoolean(AuthErrors.OAuthUserIdDoesNotMatch)(transaction.email == profile.email)
        _ ← fromDBIO(persist.OAuth2Token.createOrUpdate(token))

        _ ← fromDBIO(AuthTransaction.updateSetChecked(transactionHash))

        email ← fromDBIOOption(AuthErrors.EmailUnoccupied)(persist.UserEmail.find(transaction.email))

        user ← authorizeT(email.userId, profile.locale.getOrElse(""), clientData)
        userStruct ← fromDBIO(userStruct(user, None, clientData.authId))

        //refresh session data
        authSession = models.AuthSession(
          userId = user.id,
          id = nextIntId(ThreadLocalRandom.current()),
          authId = clientData.authId,
          appId = transaction.appId,
          appTitle = models.AuthSession.appTitleOf(transaction.appId),
          deviceHash = transaction.deviceHash,
          deviceTitle = transaction.deviceTitle,
          authTime = DateTime.now,
          authLocation = "",
          latitude = None,
          longitude = None
        )
        _ ← fromDBIO(refreshAuthSession(user.id, transaction.deviceHash, authSession))
        _ ← fromDBIO(persist.auth.AuthTransaction.delete(transactionHash))
        ack ← fromFuture(authorizeSession(user.id, clientData))
      } yield ResponseAuth(userStruct, misc.Config(maxGroupSize))
    db.run(action.run.transactionally)
  }

  def jhandleGetOAuth2Params(transactionHash: String, redirectUrl: String, clientData: ClientData): Future[HandlerResult[ResponseGetOAuth2Params]] = {
    val action =
      for {
        transaction ← fromDBIOOption(AuthErrors.InvalidAuthTransaction)(persist.auth.AuthEmailTransaction.find(transactionHash))
        url ← fromOption(AuthErrors.RedirectUrlInvalid)(oauth2Service.getAuthUrl(redirectUrl, transaction.email))
        _ ← fromDBIO(persist.auth.AuthEmailTransaction.updateRedirectUri(transaction.transactionHash, redirectUrl))
      } yield ResponseGetOAuth2Params(url)
    db.run(action.run.transactionally)
  }

  def jhandleStartPhoneAuth(phoneNumber: Long, appId: Int, apiKey: String, deviceHash: Array[Byte], deviceTitle: String, clientData: ClientData): Future[HandlerResult[ResponseStartPhoneAuth]] = {
    val action = for {
      normalizedPhone ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeLong(phoneNumber))
      optAuthTransaction ← fromDBIO(persist.auth.AuthPhoneTransaction.findByPhone(normalizedPhone))
      transactionHash ← optAuthTransaction match {
        case Some(transaction) ⇒ point(transaction.transactionHash)
        case None ⇒
          val code = genSmsCode(normalizedPhone)
          val accessSalt = ACLUtils.nextAccessSalt()
          val transactionHash = ACLUtils.authTransactionHash(accessSalt)
          val phoneAuthTransaction = models.AuthPhoneTransaction(normalizedPhone, transactionHash, appId, apiKey, deviceHash, deviceTitle, accessSalt)
          for {
            _ ← fromDBIO(persist.AuthCode.create(phoneAuthTransaction.transactionHash, code))
            _ ← fromDBIO(persist.auth.AuthPhoneTransaction.create(phoneAuthTransaction))
            _ ← point(sendSmsCode(clientData.authId, normalizedPhone, code))
          } yield transactionHash
      }
      isRegistered ← fromDBIO(persist.UserPhone.exists(normalizedPhone))
    } yield ResponseStartPhoneAuth(transactionHash, isRegistered)
    db.run(action.run.transactionally)
  }

  def jhandleSignUp(transactionHash: String, name: String, sex: Option[Sex], clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        //retreive `authTransaction`
        transaction ← fromDBIOOption(AuthErrors.InvalidAuthTransaction)(persist.auth.AuthTransaction.findChildren(transactionHash))

        //ensure that `authTransaction` is checked
        _ ← fromOption(AuthErrors.NotValidated)(transaction.isChecked.option("")) //TODO: make it more clear

        signInORsignUp ← transaction match {
          case p: models.AuthPhoneTransaction ⇒ newUserPhoneSignUp(p, name, sex)
          case e: models.AuthEmailTransaction ⇒ newUserEmailSignUp(e, name, sex)
        }

        //fallback to sign up if user exists
        user ← signInORsignUp match {
          case -\/((userId, countryCode)) ⇒ authorizeT(userId, countryCode, clientData)
          case \/-(user)                  ⇒ handleUserCreate(user, transaction, clientData.authId)
        }
        userStruct ← fromDBIO(userStruct(user, None, clientData.authId))

        //refresh session data
        authSession = models.AuthSession(
          userId = user.id,
          id = nextIntId(ThreadLocalRandom.current()),
          authId = clientData.authId,
          appId = transaction.appId,
          appTitle = models.AuthSession.appTitleOf(transaction.appId),
          deviceHash = transaction.deviceHash,
          deviceTitle = transaction.deviceTitle,
          authTime = DateTime.now,
          authLocation = "",
          latitude = None,
          longitude = None
        )
        _ ← fromDBIO(refreshAuthSession(user.id, transaction.deviceHash, authSession))
        ack ← fromFuture(authorizeSession(user.id, clientData))
      } yield ResponseAuth(userStruct, misc.Config(maxGroupSize))
    db.run(action.run.transactionally)
  }

  def jhandleStartEmailAuth(email: String, appId: Int, apiKey: String, deviceHash: Array[Byte], deviceTitle: String, clientData: ClientData): Future[HandlerResult[ResponseStartEmailAuth]] = {
    val action = for {
      validEmail ← fromEither(validEmail(email).leftMap(validationFailed("EMAIL_INVALID", _))) //it actually does not change input email
      activationType = if (OAuth2ProvidersDomains.supportsOAuth2(email)) OAUTH2 else CODE
      isRegistered ← fromDBIO(persist.UserEmail.exists(validEmail))
      optTransaction ← fromDBIO(persist.auth.AuthEmailTransaction.findByEmail(validEmail))
      transactionHash ← optTransaction match {
        case Some(trans) ⇒ point(trans.transactionHash)
        case None ⇒
          val accessSalt = ACLUtils.nextAccessSalt()
          val transactionHash = ACLUtils.authTransactionHash(accessSalt)
          val emailAuthTransaction = models.AuthEmailTransaction(validEmail, None, transactionHash, appId, apiKey, deviceHash, deviceTitle, accessSalt)
          val code = genCode()
          activationType match {
            case CODE ⇒
              for {
                _ ← fromDBIO(persist.AuthCode.create(emailAuthTransaction.transactionHash, code))
                _ ← fromDBIO(persist.auth.AuthEmailTransaction.create(emailAuthTransaction))
                _ ← point(sendEmailCode(clientData.authId, email, code))
              } yield transactionHash
            case OAUTH2 ⇒
              for {
                _ ← fromDBIO(persist.auth.AuthEmailTransaction.create(emailAuthTransaction))
              } yield transactionHash
          }
      }
    } yield ResponseStartEmailAuth(transactionHash, isRegistered, activationType)
    db.run(action.run.transactionally)
  }

  //TODO: add email code validation
  def jhandleValidateCode(transactionHash: String, code: String, clientData: ClientData): Future[HandlerResult[ResponseAuth]] = {
    val action: Result[ResponseAuth] =
      for {
        //retreive `authTransaction`
        transaction ← fromDBIOOption(AuthErrors.InvalidAuthTransaction)(persist.auth.AuthTransaction.findChildren(transactionHash))

        //validate code
        userAndCounty ← validateCode(transaction, code)
        (userId, countryCode) = userAndCounty

        //sign in user and delete auth transaction
        user ← authorizeT(userId, countryCode, clientData)
        userStruct ← fromDBIO(userStruct(user, None, clientData.authId))
        _ ← fromDBIO(persist.auth.AuthTransaction.delete(transaction.transactionHash))

        //refresh session data
        authSession = models.AuthSession(
          userId = user.id,
          id = nextIntId(ThreadLocalRandom.current()),
          authId = clientData.authId,
          appId = transaction.appId,
          appTitle = models.AuthSession.appTitleOf(transaction.appId),
          deviceHash = transaction.deviceHash,
          deviceTitle = transaction.deviceTitle,
          authTime = DateTime.now,
          authLocation = "",
          latitude = None,
          longitude = None
        )
        _ ← fromDBIO(refreshAuthSession(user.id, transaction.deviceHash, authSession))
        ack ← fromFuture(authorizeSession(user.id, clientData))
      } yield ResponseAuth(userStruct, misc.Config(maxGroupSize))
    db.run(action.run.transactionally)
  }

  override def jhandleSignOut(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData) map { implicit client ⇒
      persist.AuthSession.findByAuthId(client.authId) flatMap {
        case Some(session) ⇒
          for (_ ← logout(session)) yield Ok(misc.ResponseVoid)
        case None ⇒ throw new Exception(s"Cannot find AuthSession for authId: ${client.authId}")
      }
    }

    db.run(toDBIOAction(action))
  }

  override def jhandleTerminateAllSessions(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      for {
        sessions ← persist.AuthSession.findByUserId(client.userId) map (_.filterNot(_.authId == client.authId))
        _ ← DBIO.sequence(sessions map logout)
      } yield {
        Ok(ResponseVoid)
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleTerminateSession(id: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      persist.AuthSession.find(client.userId, id).headOption flatMap {
        case Some(session) ⇒
          for (_ ← logout(session)) yield Ok(ResponseVoid)
        case None ⇒
          DBIO.successful(Error(AuthErrors.AuthSessionNotFound))
      }

    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

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
    PhoneNumber.normalizeLong(rawPhoneNumber) match {
      case None ⇒
        Future.successful(Error(AuthErrors.PhoneNumberInvalid))
      case Some(normPhoneNumber) ⇒
        val action = persist.AuthSmsCodeObsolete.findByPhoneNumber(normPhoneNumber).headOption.flatMap {
          case Some(models.AuthSmsCodeObsolete(_, _, smsHash, smsCode, _)) ⇒
            DBIO.successful(normPhoneNumber :: smsHash :: smsCode :: HNil)
          case None ⇒
            val smsHash = genSmsHash()
            val smsCode = genSmsCode(normPhoneNumber)
            for (
              _ ← persist.AuthSmsCodeObsolete.create(
                id = ThreadLocalRandom.current().nextLong(),
                phoneNumber = normPhoneNumber,
                smsHash = smsHash,
                smsCode = smsCode
              )
            ) yield (normPhoneNumber :: smsHash :: smsCode :: HNil)
        }.flatMap { res ⇒
          persist.UserPhone.exists(normPhoneNumber) map (res :+ _)
        }.map {
          case number :: smsHash :: smsCode :: isRegistered :: HNil ⇒
            sendSmsCode(clientData.authId, number, smsCode)
            Ok(ResponseSendAuthCodeObsolete(smsHash, isRegistered))
        }
        db.run(action.transactionally)
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
    normalizeWithCountry(rawPhoneNumber) match {
      case None ⇒ Future.successful(Error(AuthErrors.PhoneNumberInvalid))
      case Some((normPhoneNumber, countryCode)) ⇒
        if (smsCode.isEmpty) Future.successful(Error(AuthErrors.PhoneCodeEmpty))
        else {
          val action = (for {
            optCode ← persist.AuthSmsCodeObsolete.findByPhoneNumber(normPhoneNumber).headOption
            optPhone ← persist.UserPhone.findByPhoneNumber(normPhoneNumber).headOption
          } yield (optCode :: optPhone :: HNil)).flatMap {
            case None :: _ :: HNil ⇒ DBIO.successful(Error(AuthErrors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsHash != smsHash ⇒
              DBIO.successful(Error(AuthErrors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsCode != smsCode ⇒
              DBIO.successful(Error(AuthErrors.PhoneCodeInvalid))
            case Some(_) :: optPhone :: HNil ⇒
              signType match {
                case Up(rawName, isSilent) ⇒
                  persist.AuthSmsCodeObsolete.deleteByPhoneNumber(normPhoneNumber).andThen(
                    optPhone match {
                      // Phone does not exist, register the user
                      case None ⇒ withValidName(rawName) { name ⇒
                        val rnd = ThreadLocalRandom.current()
                        val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
                        val user = models.User(userId, ACLUtils.nextAccessSalt(rnd), name, countryCode, models.NoSex, models.UserState.Registered, LocalDateTime.now(ZoneOffset.UTC))

                        for {
                          _ ← persist.User.create(user)
                          _ ← persist.UserPhone.create(phoneId, userId, ACLUtils.nextAccessSalt(rnd), normPhoneNumber, "Mobile phone")
                          _ ← persist.AuthId.setUserData(clientData.authId, userId)
                          _ ← persist.AvatarData.create(models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong))
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
                      persist.AuthSmsCodeObsolete.deleteByPhoneNumber(normPhoneNumber).andThen(
                        signIn(clientData.authId, phone.userId, countryCode, clientData)
                      )
                  }
              }
          }.flatMap {
            case \/-(user :: HNil) ⇒
              val rnd = ThreadLocalRandom.current()
              val authSession = models.AuthSession(
                userId = user.id,
                id = nextIntId(rnd),
                authId = clientData.authId,
                appId = appId,
                appTitle = models.AuthSession.appTitleOf(appId),
                deviceHash = deviceHash,
                deviceTitle = deviceTitle,
                authTime = DateTime.now,
                authLocation = "",
                latitude = None,
                longitude = None
              )

              for {
                prevSessions ← persist.AuthSession.findByUserIdAndDeviceHash(user.id, deviceHash)
                _ ← DBIO.sequence(prevSessions map logout)
                _ ← persist.AuthSession.create(authSession)
                _ ← signType match {
                  case Up(_, isSilent) ⇒ markContactRegistered(user, normPhoneNumber, isSilent)
                  case _               ⇒ DBIO.successful(())
                }
                userStruct ← userStruct(
                  user,
                  None,
                  clientData.authId
                )
              } yield {
                Ok(
                  ResponseAuth(
                    userStruct,
                    misc.Config(maxGroupSize)
                  )
                )
              }
            case error @ -\/(_) ⇒ DBIO.successful(error)
          }

          for (result ← db.run(action.transactionally)) yield {
            result match {
              case Ok(r: ResponseAuth) ⇒
                sessionRegion.ref ! SessionMessage.envelope(SessionMessage.AuthorizeUser(r.user.id))(clientData)
              case _ ⇒
            }

            result
          }
        }
    }
  }

  private def signIn(authId: Long, userId: Int, countryCode: String, clientData: ClientData) = {
    persist.User.find(userId).headOption.flatMap {
      case None ⇒ throw new Exception("Failed to retrieve user")
      case Some(user) ⇒
        for {
          _ ← persist.User.setCountryCode(userId = userId, countryCode = countryCode)
          _ ← persist.AuthId.setUserData(authId, userId)
        } yield \/-(user :: HNil)
    }
  }

}
