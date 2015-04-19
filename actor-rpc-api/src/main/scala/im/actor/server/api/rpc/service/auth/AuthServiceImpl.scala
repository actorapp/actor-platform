package im.actor.server.api.rpc.service.auth


import scala.concurrent._, forkjoin.ThreadLocalRandom
import scalaz._

import akka.actor.{ ActorRef, ActorSystem }
import org.joda.time.DateTime
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist
import im.actor.server.session._

class AuthServiceImpl(sessionRegion: SessionRegion)(implicit val actorSystem: ActorSystem, val db: Database) extends AuthService with Helpers {
  private trait SignType
  private case class Up(name: String, isSilent: Boolean) extends SignType
  private case object In extends SignType

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  object Errors {
    val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
    val PhoneNumberUnoccupied = RpcError(400, "PHONE_NUMBER_UNOCCUPIED", "", false, None)
    val PhoneCodeEmpty = RpcError(400, "PHONE_CODE_EMPTY", "", false, None)
    val PhoneCodeExpired = RpcError(400, "PHONE_CODE_EXPIRED", "", false, None)
    val PhoneCodeInvalid = RpcError(400, "PHONE_CODE_INVALID", "", false, None)
    val InvalidKey = RpcError(400, "INVALID_KEY", "", false, None)
  }

  override def jhandleGetAuthSessions(clientData: ClientData): Future[HandlerResult[ResponseGetAuthSessions]] =
    throw new NotImplementedError()

  override def jhandleSendAuthCode(
    rawPhoneNumber: Long, appId: Int, apiKey: String, clientData: ClientData
  ): Future[HandlerResult[ResponseSendAuthCode]] = {
    util.PhoneNumber.normalizeLong(rawPhoneNumber) match {
      case None =>
        Future.successful(Error(Errors.PhoneNumberInvalid))
      case Some(normPhoneNumber) =>
        val action = persist.AuthSmsCode.findByPhoneNumber(normPhoneNumber).headOption.flatMap {
          case Some(models.AuthSmsCode(_, smsHash, smsCode)) =>
            DBIO.successful(normPhoneNumber :: smsHash :: smsCode :: HNil)
          case None =>
            val smsHash = genSmsHash()
            val smsCode = normPhoneNumber.toString match {
              case strNumber if strNumber.startsWith("7555") => strNumber(4).toString * 4
              case _                                         => genSmsCode()
            }
            for (
              _ <- persist.AuthSmsCode.create(
                phoneNumber = normPhoneNumber, smsHash = smsHash, smsCode = smsCode
              )
            ) yield (normPhoneNumber :: smsHash :: smsCode :: HNil)
        }.flatMap { res =>
          persist.UserPhone.exists(normPhoneNumber) map (res :+ _)
        }.map {
          case number :: smsHash :: smsCode :: isRegistered :: HNil =>
            sendSmsCode(clientData.authId, number, smsCode)
            Ok(ResponseSendAuthCode(smsHash, isRegistered))
        }
        db.run(action.transactionally)
    }
  }

  override def jhandleSendAuthCall(
    phoneNumber: Long, smsHash: String, appId: Int, apiKey: String, clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def jhandleSignOut(clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData) map { implicit client =>
      DBIO.successful(Ok(misc.ResponseVoid))
    }

    db.run(toDBIOAction(action))
  }


  override def jhandleSignIn(
    rawPhoneNumber: Long,
    smsHash: String,
    smsCode: String,
    publicKey: Array[Byte],
    deviceHash: Array[Byte],
    deviceTitle: String,
    appId: Int,
    appKey: String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(
      In,
      rawPhoneNumber, smsHash, smsCode,
      publicKey, deviceHash, deviceTitle, appId, appKey,
      clientData
    )

  override def jhandleSignUp(
    rawPhoneNumber: Long,
    smsHash: String,
    smsCode: String,
    name: String,
    publicKey: Array[Byte],
    deviceHash: Array[Byte],
    deviceTitle: String,
    appId: Int,
    appKey: String,
    isSilent: Boolean,
    clientData: ClientData
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(
      Up(name, isSilent),
      rawPhoneNumber, smsHash, smsCode,
      publicKey, deviceHash, deviceTitle, appId, appKey,
      clientData
    )

  private def handleSign(
    signType: SignType,
    rawPhoneNumber: Long,
    smsHash: String,
    smsCode: String,
    rawPublicKey: Array[Byte],
    deviceHash: Array[Byte],
    deviceTitle: String,
    appId: Int,
    appKey: String,
    clientData: ClientData
  ): Future[HandlerResult[ResponseAuth]] = {
    util.PhoneNumber.normalizeWithCountry(rawPhoneNumber) match {
      case None => Future.successful(Error(Errors.PhoneNumberInvalid))
      case Some((normPhoneNumber, countryCode)) =>
        if (smsCode.isEmpty) Future.successful(Error(Errors.PhoneCodeEmpty))
        else if (rawPublicKey.length == 0) Future.successful(Error(Errors.InvalidKey))
        else {
          val action = (for {
            optCode <- persist.AuthSmsCode.findByPhoneNumber(normPhoneNumber).headOption
            optPhone <- persist.UserPhone.findByPhoneNumber(normPhoneNumber).headOption
          } yield (optCode :: optPhone :: HNil)).flatMap {
            case None :: _ :: HNil => DBIO.successful(Error(Errors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsHash != smsHash =>
              DBIO.successful(Error(Errors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsCode != smsCode =>
              DBIO.successful(Error(Errors.PhoneCodeInvalid))
            case Some(_) :: optPhone :: HNil =>
              signType match {
                case Up(rawName, isSilent) =>
                  persist.AuthSmsCode.deleteByPhoneNumber(normPhoneNumber).andThen(
                    optPhone match {
                      // Phone does not exist, register the user
                      case None => withValidName(rawName) { name =>
                        withValidPublicKey(rawPublicKey) { publicKey =>
                          val rnd = ThreadLocalRandom.current()
                          val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
                          val user = models.User(userId, nextAccessSalt(rnd), name, countryCode, models.NoSex, models.UserState.Registered)

                          for {
                            _ <- persist.User.create(user)
                            _ <- persist.UserPhone.create(phoneId, userId, nextAccessSalt(rnd), normPhoneNumber, "Mobile phone")
                            pkHash = keyHash(publicKey)
                            _ <- persist.UserPublicKey.create(models.UserPublicKey(userId, pkHash, publicKey))
                            _ <- persist.AuthId.setUserData(clientData.authId, userId, pkHash)
                            _ <- persist.AvatarData.create(models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong))
                          } yield {
                            \/-(user :: pkHash :: HNil)
                          }
                        }
                      }
                      // Phone already exists, fall back to SignIn
                      case Some(phone) =>
                        withValidPublicKey(rawPublicKey) { publicKey =>
                          signIn(clientData.authId, phone.userId, publicKey, keyHash(publicKey), countryCode, clientData)
                        }
                    }
                  )
                case In =>
                  withValidPublicKey(rawPublicKey) { publicKey =>
                    optPhone match {
                      case None => DBIO.successful(Error(Errors.PhoneNumberUnoccupied))
                      case Some(phone) =>
                        persist.AuthSmsCode.deleteByPhoneNumber(normPhoneNumber).andThen(
                          signIn(clientData.authId, phone.userId, publicKey, keyHash(publicKey), countryCode, clientData)
                        )
                    }
                  }
              }
          }.flatMap {
            case \/-(user :: pkHash :: HNil) =>
              val rnd = ThreadLocalRandom.current()
              val authSession = models.AuthSession(
                userId = user.id,
                id = nextIntId(rnd),
                authId = clientData.authId,
                appId = appId,
                appTitle = models.AuthSession.appTitleOf(appId),
                publicKeyHash = pkHash,
                deviceHash = deviceHash,
                deviceTitle = deviceTitle,
                authTime = DateTime.now,
                authLocation = "",
                latitude = None,
                longitude = None
              )

              for {
                prevSessions <- persist.AuthSession.findByUserIdAndDeviceHash(user.id, deviceHash)
                _ <- DBIO.sequence(prevSessions map { s =>
                  persist.AuthSession.delete(user.id, s.id) andThen persist.AuthId.delete(s.authId)
                })
                _ <- persist.AuthSession.create(authSession)
                userStruct <- util.UserUtils.userStruct(
                  user,
                  None,
                  clientData.authId
                )
              } yield {
                Ok(
                  ResponseAuth(
                    pkHash,
                    userStruct,
                    misc.Config(300)
                  )
                )
              }
            case error @ -\/(_) => DBIO.successful(error)
          }

          for (result <- db.run(action.transactionally))
            yield {
              result match {
                case Ok(r: ResponseAuth) =>
                  sessionRegion.ref ! SessionMessage.envelope(SessionMessage.UserAuthorized(r.user.id))(clientData)
                case _ =>
              }

              result
            }
        }
    }
  }

  override def jhandleTerminateAllSessions(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def jhandleTerminateSession(id: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  private def signIn(authId: Long, userId: Int, pkData: Array[Byte], pkHash: Long, countryCode: String, clientData: ClientData) = {
    persist.User.find(userId).headOption.flatMap {
      case None => throw new Exception("Failed to retrieve user")
      case Some(user) =>
        (for {
          _ <- persist.User.setCountryCode(userId = userId, countryCode = countryCode)
          _ <- persist.AuthId.setUserData(authId, userId, pkHash)
          pkOpt <- persist.UserPublicKey.find(user.id, pkHash).headOption
        } yield pkOpt).flatMap {
          case None =>
            for (
              _ <- persist.UserPublicKey.create(models.UserPublicKey(userId = userId, hash = pkHash, data = pkData))
            ) yield \/-(user :: pkHash :: HNil)
          case Some(pk) =>
            if (!pkData.sameElements(pk.data)) {
              throw new Exception("Public key with the same hash already exists but not match with request's key data")
            } else {
              DBIO.successful(\/-(user :: pkHash :: HNil))
            }
        }
    }
  }

  private def sendSmsCode(authId: Long, phoneNumber: Long, code: String): Unit = {

  }

  private def genSmsCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c => c == '0' || c == '-').take(6)

  private def genSmsHash() = ThreadLocalRandom.current.nextLong().toString
}
