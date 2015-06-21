package im.actor.server.api.rpc.service.auth

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scalaz._

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.util.Timeout
import org.joda.time.DateTime
import shapeless._
import slick.dbio
import slick.dbio.DBIO
import slick.dbio.Effect.Write
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.misc._
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.session._
import im.actor.server.sms.ActivationContext
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.PhoneNumber.normalizeWithCountry
import im.actor.server.util._
import im.actor.server.{ models, persist }

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

class AuthServiceImpl(activationContext: ActivationContext, mediator: ActorRef)(
  implicit
  val sessionRegion:           SessionRegion,
  val seqUpdatesManagerRegion: SeqUpdatesManagerRegion,
  val socialManagerRegion:     SocialManagerRegion,
  val actorSystem:             ActorSystem,
  val db:                      Database
) extends AuthService with Helpers {

  import AnyRefLogSource._
  import IdUtils._
  import SeqUpdatesManager._
  import SocialManager._

  private trait SignType

  private case class Up(name: String, isSilent: Boolean) extends SignType

  private case object In extends SignType

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  implicit private val timeout = Timeout(5.seconds) // TODO: configurable

  private val log = Logging(actorSystem, this)

  object Errors {
    val AuthSessionNotFound = RpcError(404, "AUTH_SESSION_NOT_FOUND", "Auth session not found.", false, None)
    val InvalidKey = RpcError(400, "INVALID_KEY", "Invalid key.", false, None)
    val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
    val PhoneNumberUnoccupied = RpcError(400, "PHONE_NUMBER_UNOCCUPIED", "", false, None)
    val PhoneCodeEmpty = RpcError(400, "PHONE_CODE_EMPTY", "Code is empty.", false, None)
    val PhoneCodeExpired = RpcError(400, "PHONE_CODE_EXPIRED", "Code is expired.", false, None)
    val PhoneCodeInvalid = RpcError(400, "PHONE_CODE_INVALID", "Invalid code.", false, None)
  }

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

  override def jhandleSendAuthCode(
    rawPhoneNumber: Long,
    appId:          Int,
    apiKey:         String,
    clientData:     ClientData
  ): Future[HandlerResult[ResponseSendAuthCode]] = {
    PhoneNumber.normalizeLong(rawPhoneNumber) match {
      case None ⇒
        Future.successful(Error(Errors.PhoneNumberInvalid))
      case Some(normPhoneNumber) ⇒
        val action = persist.AuthSmsCode.findByPhoneNumber(normPhoneNumber).headOption.flatMap {
          case Some(models.AuthSmsCode(_, smsHash, smsCode)) ⇒
            DBIO.successful(normPhoneNumber :: smsHash :: smsCode :: HNil)
          case None ⇒
            val smsHash = genSmsHash()
            val smsCode = normPhoneNumber.toString match {
              case strNumber if strNumber.startsWith("7555") ⇒ strNumber(4).toString * 4
              case _                                         ⇒ genSmsCode()
            }
            for (
              _ ← persist.AuthSmsCode.create(
                phoneNumber = normPhoneNumber, smsHash = smsHash, smsCode = smsCode
              )
            ) yield (normPhoneNumber :: smsHash :: smsCode :: HNil)
        }.flatMap { res ⇒
          persist.UserPhone.exists(normPhoneNumber) map (res :+ _)
        }.map {
          case number :: smsHash :: smsCode :: isRegistered :: HNil ⇒
            sendSmsCode(clientData.authId, number, smsCode)
            Ok(ResponseSendAuthCode(smsHash, isRegistered))
        }
        db.run(action.transactionally)
    }
  }

  override def jhandleSendAuthCall(
    phoneNumber: Long,
    smsHash:     String,
    appId:       Int,
    apiKey:      String,
    clientData:  ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    Future {
      throw new Exception("Not implemented")
    }

  override def jhandleSignIn(
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

  override def jhandleSignUp(
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
      case None ⇒ Future.successful(Error(Errors.PhoneNumberInvalid))
      case Some((normPhoneNumber, countryCode)) ⇒
        if (smsCode.isEmpty) Future.successful(Error(Errors.PhoneCodeEmpty))
        else {
          val action = (for {
            optCode ← persist.AuthSmsCode.findByPhoneNumber(normPhoneNumber).headOption
            optPhone ← persist.UserPhone.findByPhoneNumber(normPhoneNumber).headOption
          } yield (optCode :: optPhone :: HNil)).flatMap {
            case None :: _ :: HNil ⇒ DBIO.successful(Error(Errors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsHash != smsHash ⇒
              DBIO.successful(Error(Errors.PhoneCodeExpired))
            case Some(smsCodeModel) :: _ :: HNil if smsCodeModel.smsCode != smsCode ⇒
              DBIO.successful(Error(Errors.PhoneCodeInvalid))
            case Some(_) :: optPhone :: HNil ⇒
              signType match {
                case Up(rawName, isSilent) ⇒
                  persist.AuthSmsCode.deleteByPhoneNumber(normPhoneNumber).andThen(
                    optPhone match {
                      // Phone does not exist, register the user
                      case None ⇒ withValidName(rawName) { name ⇒
                        val rnd = ThreadLocalRandom.current()
                        val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
                        val user = models.User(userId, ACLUtils.nextAccessSalt(rnd), name, countryCode, models.NoSex, models.UserState.Registered)

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
                    case None ⇒ DBIO.successful(Error(Errors.PhoneNumberUnoccupied))
                    case Some(phone) ⇒
                      persist.AuthSmsCode.deleteByPhoneNumber(normPhoneNumber).andThen(
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
                userStruct ← UserUtils.userStruct(
                  user,
                  None,
                  clientData.authId
                )
              } yield {
                Ok(
                  ResponseAuth(
                    userStruct,
                    misc.Config(300)
                  )
                )
              }
            case error @ -\/(_) ⇒ DBIO.successful(error)
          }

          for (result ← db.run(action.transactionally)) yield {
            result match {
              case Ok(r: ResponseAuth) ⇒
                sessionRegion.ref ! SessionMessage.envelope(SessionMessage.UserAuthorized(r.user.id))(clientData)
              case _ ⇒
            }

            result
          }
        }
    }
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
          DBIO.successful(Error(Errors.AuthSessionNotFound))
      }

    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
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

  private def markContactRegistered(user: models.User, phoneNumber: Long, isSilent: Boolean): DBIO[Unit] = {
    val date = new DateTime

    persist.contact.UnregisteredContact.find(phoneNumber) flatMap { contacts ⇒
      // TODO: use service-level logging
      actorSystem.log.debug(s"Unregistered ${phoneNumber} is in contacts of users: $contacts")

      val randomId = ThreadLocalRandom.current().nextLong()
      val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
      // TODO: write service message if isSilent == true

      // FIXME: #perf broadcast updates using broadcastUpdateAll to serialize update once
      val actions = contacts map { contact ⇒
        for {
          _ ← persist.contact.UserContact.createOrRestore(contact.ownerUserId, user.id, phoneNumber, Some(user.name), user.accessSalt)
          _ ← broadcastUserUpdate(contact.ownerUserId, update, Some(s"${contact.name.getOrElse(user.name)} registered"))
        } yield {
          recordRelation(user.id, contact.ownerUserId)
        }
      }

      for {
        _ ← DBIO.sequence(actions)
        _ ← persist.contact.UnregisteredContact.deleteAll(phoneNumber)
      } yield ()
    }
  }

  private def logout(session: models.AuthSession): dbio.DBIOAction[Unit, NoStream, Write with Write] = {
    actorSystem.log.debug(s"Terminating AuthSession ${session.id} of user ${session.userId} and authId ${session.authId}")

    for {
      _ ← persist.AuthSession.delete(session.userId, session.id)
      _ ← persist.AuthId.delete(session.authId)
    } yield {
      AuthService.publishAuthIdInvalidated(mediator, session.authId)
    }
  }

  private def sendSmsCode(authId: Long, phoneNumber: Long, code: String): Unit = {
    if (!phoneNumber.toString.startsWith("7555")) {
      log.info("Sending code {} to {}", code, phoneNumber)
      activationContext.send(authId, phoneNumber, code)
    }
  }

  private def genSmsCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(6)

  private def genSmsHash() = ThreadLocalRandom.current.nextLong().toString
}
