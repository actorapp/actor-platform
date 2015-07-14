package im.actor.server.api.rpc.service.auth

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scalaz.{ -\/, \/, \/- }

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime
import slick.dbio
import slick.dbio.Effect.Write
import slick.dbio._

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.users.Sex._
import im.actor.server.activation.Activation.{ EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.models.{ AuthEmailTransaction, AuthPhoneTransaction, User }
import im.actor.server.persist.auth.AuthTransaction
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.session.SessionMessage.AuthorizeUserAck
import im.actor.server.session.{ SessionMessage, SessionRegion }
import im.actor.server.social.SocialManager._
import im.actor.server.util.IdUtils._
import im.actor.server.util.PhoneNumber._
import im.actor.server.util.StringUtils.validName
import im.actor.server.util._
import im.actor.server.{ models, persist }

trait AuthHelpers extends Helpers {
  self: AuthServiceImpl ⇒

  implicit private val timeout = Timeout(5.seconds)

  //expiration of code won't work
  protected def newUserPhoneSignUp(transaction: models.AuthPhoneTransaction, name: String, sex: Option[Sex]): Result[(Int, String) \/ User] = {
    val phone = transaction.phoneNumber
    for {
      optPhone ← fromDBIO(persist.UserPhone.findByPhoneNumber(phone).headOption)
      phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone))
      (_, countryCode) = phoneAndCode
      result ← optPhone match {
        case Some(userPhone) ⇒ point(-\/((userPhone.userId, countryCode)))
        case None            ⇒ newUser(name, countryCode, sex)
      }
    } yield result
  }

  protected def newUserEmailSignUp(transaction: models.AuthEmailTransaction, name: String, sex: Option[Sex]): Result[(Int, String) \/ User] = {
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
      _ ← fromDBIO(persist.User.create(user))
      _ ← fromDBIO(persist.AuthId.setUserData(authId, user.id))
      _ ← fromDBIO(persist.AvatarData.create(models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong)))

      _ ← fromDBIO(AuthTransaction.delete(transaction.transactionHash))

      _ ← transaction match {
        case p: models.AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          val rng = ThreadLocalRandom.current()
          for {
            _ ← fromDBIO(activationContext.finish(p.transactionHash))
            _ ← fromDBIO(persist.UserPhone.create(rng.nextInt(), user.id, ACLUtils.nextAccessSalt(rng), phone, "Mobile phone"))
            _ ← fromDBIO(markContactRegistered(user, phone, false))
          } yield ()
        case e: models.AuthEmailTransaction ⇒
          val rng = ThreadLocalRandom.current()
          for {
            _ ← fromDBIO(persist.UserEmail.create(rng.nextInt(), user.id, ACLUtils.nextAccessSalt(rng), e.email, "Email"))
            _ ← markContactRegistered(user, e.email, false)
          } yield ()
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
      validationResponse ← fromFuture(activationContext.validate(transactionHash, code))
      _ ← validationResponse match {
        case ExpiredCode | InvalidHash ⇒ fromEither[Unit](-\/(codeExpired))
        case InvalidCode               ⇒ fromEither[Unit](-\/(codeInvalid))
        case InvalidResponse           ⇒ fromEither[Unit](-\/(AuthErrors.ActivationServiceError))
        case Validated                 ⇒ point(())
      }
      _ ← fromDBIO(persist.auth.AuthTransaction.updateSetChecked(transactionHash))

      userAndCountry ← transaction match {
        case p: AuthPhoneTransaction ⇒
          val phone = p.phoneNumber
          for {
            //if user is not registered - return error
            phoneModel ← fromDBIOOption(AuthErrors.PhoneNumberUnoccupied)(persist.UserPhone.findByPhoneNumber(phone).headOption)
            phoneAndCode ← fromOption(AuthErrors.PhoneNumberInvalid)(normalizeWithCountry(phone))
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
   * Terminate all sessions associated with given `deviceHash` for user with id `userId`
   * and create new session
   */
  protected def refreshAuthSession(userId: Int, deviceHash: Array[Byte], newSession: models.AuthSession): DBIO[Unit] =
    for {
      prevSessions ← persist.AuthSession.findByUserIdAndDeviceHash(userId, deviceHash)
      _ ← DBIO.sequence(prevSessions map logout)
      _ ← persist.AuthSession.create(newSession)
    } yield ()

  protected def authorizeSession(userId: Int, clientData: ClientData)(implicit sessionRegion: SessionRegion): Future[AuthorizeUserAck] =
    sessionRegion.ref
      .ask(SessionMessage.envelope(SessionMessage.AuthorizeUser(userId))(clientData))
      .mapTo[SessionMessage.AuthorizeUserAck]

  //TODO: what country to use in case of email auth
  protected def authorizeT(userId: Int, countryCode: String, clientData: ClientData): Result[User] = {
    for {
      user ← fromDBIOOption(CommonErrors.UserNotFound)(persist.User.find(userId).headOption)
      _ ← fromDBIO(persist.User.setCountryCode(userId, countryCode))
      _ ← fromDBIO(persist.AuthId.setUserData(clientData.authId, userId))
    } yield user
  }

  protected def logout(session: models.AuthSession)(implicit system: ActorSystem, m: PubSubMediator): dbio.DBIOAction[Unit, NoStream, Write with Write] = {
    system.log.debug(s"Terminating AuthSession ${session.id} of user ${session.userId} and authId ${session.authId}")

    for {
      _ ← persist.AuthSession.delete(session.userId, session.id)
      _ ← persist.AuthId.delete(session.authId)
    } yield {
      AuthService.publishAuthIdInvalidated(m.mediator, session.authId)
    }
  }

  protected def markContactRegistered(user: models.User, phoneNumber: Long, isSilent: Boolean)(
    implicit
    system:                  ActorSystem,
    seqUpdatesManagerRegion: SeqUpdatesManagerRegion
  ): DBIO[Unit] = {
    val date = new DateTime

    persist.contact.UnregisteredPhoneContact.find(phoneNumber) flatMap { contacts ⇒
      log.debug(s"Unregistered ${phoneNumber} is in contacts of users: $contacts")
      val randomId = ThreadLocalRandom.current().nextLong()
      val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
      val serviceMessage = ServiceMessages.contactRegistered(user.id)
      // FIXME: #perf broadcast updates using broadcastUpdateAll to serialize update once
      val actions = contacts map { contact ⇒
        for {
          _ ← persist.contact.UserPhoneContact.createOrRestore(contact.ownerUserId, user.id, phoneNumber, Some(user.name), user.accessSalt)
          _ ← broadcastUserUpdate(contact.ownerUserId, update, Some(s"${contact.name.getOrElse(user.name)} registered"))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(user.id),
            models.Peer.privat(contact.ownerUserId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          recordRelation(user.id, contact.ownerUserId)
        }
      }

      for {
        _ ← DBIO.sequence(actions)
        _ ← persist.contact.UnregisteredPhoneContact.deleteAll(phoneNumber)
      } yield ()
    }
  }

  protected def markContactRegistered(user: models.User, email: String, isSilent: Boolean)(
    implicit
    system:                  ActorSystem,
    seqUpdatesManagerRegion: SeqUpdatesManagerRegion
  ): Result[Unit] = {
    val date = new DateTime
    for {
      contacts ← fromDBIO(persist.contact.UnregisteredEmailContact.find(email))
      _ = log.debug(s"Unregistered $email is in contacts of users: $contacts")
      _ ← fromDBIO(DBIO.sequence(contacts.map { contact ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        val serviceMessage = ServiceMessages.contactRegistered(user.id)
        val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        for {
          _ ← persist.contact.UserEmailContact.createOrRestore(contact.ownerUserId, user.id, email, Some(user.name), user.accessSalt)
          _ ← broadcastUserUpdate(contact.ownerUserId, update, Some(s"${contact.name.getOrElse(user.name)} registered"))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(user.id),
            models.Peer.privat(contact.ownerUserId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield recordRelation(user.id, contact.ownerUserId)
      }))
      _ ← fromDBIO(persist.contact.UnregisteredEmailContact.deleteAll(email))
    } yield ()
  }

  protected def sendSmsCode(phoneNumber: Long, code: String, transactionHash: Option[String])(implicit system: ActorSystem): DBIO[String \/ Unit] = {
    log.info("Sending code {} to {}", code, phoneNumber)
    activationContext.send(transactionHash, SmsCode(phoneNumber, code))
  }

  protected def sendEmailCode(email: String, code: String, transactionHash: String)(implicit system: ActorSystem): DBIO[String \/ Unit] = {
    log.info("Sending code {} to {}", code, email)
    activationContext.send(Some(transactionHash), EmailCode(email, code))
  }

  //TODO move to utils
  protected def genCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(6)

  protected def genSmsHash() = ThreadLocalRandom.current.nextLong().toString

  protected def genSmsCode(phone: Long): String = phone.toString match {
    case strNumber if strNumber.startsWith("7555") ⇒ strNumber(4).toString * 4
    case _                                         ⇒ genCode()
  }

  private def newUser(name: String, countryCode: String, optSex: Option[Sex]): Result[\/-[User]] = {
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

}
