package im.actor.server.api.rpc.service.auth

import akka.actor.ActorSystem

import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.server.api.util
import im.actor.server.models
import im.actor.server.persist

import org.joda.time.DateTime

import scala.concurrent._, forkjoin.ThreadLocalRandom

import scalaz._, std.either._
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

trait AuthServiceImpl extends AuthService with Helpers {
  private trait SignType
  private case class Up(name: String, isSilent: Boolean) extends SignType
  private case object In extends SignType

  val db: Database
  implicit val actorSystem: ActorSystem

  override def handleGetAuthSessions(clientData: ClientData): Future[HandlerResult[ResponseGetAuthSessions]] =
    throw new NotImplementedError()

  override def handleSendAuthCode(
    clientData: ClientData, rawPhoneNumber: Long, appId: Int, apiKey: String
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
            Ok(ResponseSendAuthCode(smsHash, isRegistered), Vector.empty)
        }
        db.run(action.transactionally)
    }
  }

  override def handleSendAuthCall(
    clientData: ClientData, phoneNumber: Long, smsHash: String, appId: Int, apiKey: String
  ): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleSignOut(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleSignIn(
    clientData: ClientData,
    rawPhoneNumber: Long,
    smsHash:     String,
    smsCode:     String,
    publicKey:   Array[Byte],
    deviceHash:  Array[Byte],
    deviceTitle: String,
    appId:       Int,
    appKey:      String
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(In,
      clientData, rawPhoneNumber, smsHash, smsCode,
      publicKey, deviceHash, deviceTitle, appId, appKey
    )

  override def handleSignUp(
    clientData:     ClientData,
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    name:           String,
    publicKey:      Array[Byte],
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String,
    isSilent:       Boolean
  ): Future[HandlerResult[ResponseAuth]] =
    handleSign(Up(name, isSilent),
      clientData, rawPhoneNumber, smsHash, smsCode,
      publicKey, deviceHash, deviceTitle, appId, appKey
    )

  private def handleSign(
    signType:       SignType,
    clientData:     ClientData,
    rawPhoneNumber: Long,
    smsHash:        String,
    smsCode:        String,
    rawPublicKey:   Array[Byte],
    deviceHash:     Array[Byte],
    deviceTitle:    String,
    appId:          Int,
    appKey:         String
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
                      case None => withValidName(rawName) { name => withValidPublicKey(rawPublicKey) { publicKey =>
                        val rnd = ThreadLocalRandom.current()
                        val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
                        val user = models.User(userId, nextAccessSalt(rnd), name, countryCode, models.NoSex, models.UserState.Registered)

                        for {
                          _ <- persist.User.create(user)
                          _ <- persist.UserPhone.create(phoneId, userId, nextAccessSalt(rnd), normPhoneNumber, "Mobile phone")
                          pkHash = keyHash(publicKey)
                          _ <- persist.UserPublicKey.create(models.UserPublicKey(userId, pkHash, publicKey, clientData.authId))
                          _ <- persist.AuthId.setUserId(clientData.authId, userId)
                          _ <- persist.AvatarData.create(models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong))
                        } yield {
                          \/-(user :: pkHash :: HNil)
                        }
                      }}
                      // Phone already exists, fall back to SignIn
                      case Some(phone) =>
                        withValidPublicKey(rawPublicKey) { publicKey =>
                          signIn(clientData.authId, phone.userId, publicKey, keyHash(publicKey), countryCode)
                        }
                    }
                  )
                case In =>
                  withValidPublicKey(rawPublicKey) { publicKey =>
                    optPhone match {
                      case None => DBIO.successful(Error(Errors.PhoneNumberUnoccupied))
                      case Some(phone) =>
                        persist.AuthSmsCode.deleteByPhoneNumber(normPhoneNumber).andThen(
                          signIn(clientData.authId, phone.userId, publicKey, keyHash(publicKey), countryCode)
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
              // TODO: logout other auth sessions

              persist.AuthSession.create(authSession) andThen util.User.struct(
                user,
                None,
                clientData.authId
              ) map { userStruct =>
                Ok(
                  ResponseAuth(
                    pkHash,
                    userStruct,
                    misc.Config(300)
                  ),
                  Vector.empty
                )
              }
            case error @ -\/(_) => DBIO.successful(error)
          }

          db.run(action)
        }
    }
  }

  override def handleTerminateAllSessions(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleTerminateSession(clientData: ClientData, id: Int): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  private def signIn(authId: Long, userId: Int, pkData: Array[Byte], pkHash: Long, countryCode: String) = {
    persist.User.find(userId).headOption.flatMap {
      case None => DBIO.successful(Error(Errors.Internal))
      case Some(user) =>
        (for {
          _ <- persist.User.setCountryCode(userId = userId, countryCode = countryCode)
          _ <- persist.AuthId.setUserId(authId = authId, userId = userId)
          pkOpt <- persist.UserPublicKey.find(userId = userId, authId = authId).headOption
        } yield pkOpt).flatMap {
          case None =>
            for (
              _ <- persist.UserPublicKey.create(models.UserPublicKey(userId = userId, hash = pkHash, data = pkData, authId = authId))
            ) yield \/-(user :: pkHash :: HNil)
          case Some(pk) =>
            if (!pkData.sameElements(pk.data)) {
              for {
                _ <- persist.UserPublicKey.delete(userId = userId, hash = pk.hash)
                _ <- persist.UserPublicKey.create(models.UserPublicKey(userId = userId, hash = pkHash, data = pkData, authId = authId))
              } yield \/-(user :: pkHash :: HNil)
            } else
              DBIO.successful(\/-(user :: pkHash :: HNil))
        }
    }
  }

  private def sendSmsCode(authId: Long, phoneNumber: Long, code: String): Unit = {

  }

  private def genSmsCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c => c == '0' || c == '-').take(6)

  private def genSmsHash() = ThreadLocalRandom.current.nextLong().toString
}
