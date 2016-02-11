package im.actor.server.api.rpc.service.profile

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.FutureResultRpc._
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileStorageExtension, FileErrors, FileStorageAdapter, ImageUtils }
import im.actor.server.persist.UserRepo
import im.actor.server.sequence.{ SequenceErrors, SeqState }
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user._
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.StringUtils
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

object ProfileRpcErrors {
  val NicknameInvalid = RpcError(400, "NICKNAME_INVALID",
    "Invalid nickname. Valid nickname should contain from 5 to 32 characters, and may consist of latin characters, numbers and underscores", false, None)
  val NameInvalid = RpcError(400, "NAME_INVALID", "Invalid name. Valid name should not be empty or contain bad symbols", false, None)
  val NicknameBusy = RpcError(400, "NICKNAME_BUSY", "This nickname already belongs some other user, we are sorry!", false, None)
  val AboutTooLong = RpcError(400, "ABOUT_TOO_LONG",
    "About is too long. It should be no longer then 255 characters", false, None)
}

final class ProfileServiceImpl()(implicit system: ActorSystem) extends ProfileService {

  import FileHelpers._
  import ImageUtils._

  override implicit val ec: ExecutionContext = system.dispatcher

  private implicit val timeout = Timeout(5.seconds)
  // TODO: configurable
  private val db: Database = DbExtension(system).db
  private val userExt = UserExtension(system)
  private implicit val socialRegion: SocialManagerRegion = SocialExtension(system).region
  private implicit val fsAdapter: FileStorageAdapter = FileStorageExtension(system).fsAdapter

  // TODO: flatten
  override def doHandleEditAvatar(fileLocation: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditAvatar]] =
    authorized(clientData) { implicit client ⇒
      val action = withFileLocation(fileLocation, AvatarSizeLimit) {
        scaleAvatar(fileLocation.fileId, ThreadLocalSecureRandom.current()) flatMap {
          case Right(avatar) ⇒
            for {
              UserCommands.UpdateAvatarAck(avatar, SeqState(seq, state)) ← DBIO.from(userExt.updateAvatar(client.userId, Some(avatar)))
            } yield Ok(ResponseEditAvatar(
              avatar.get,
              seq,
              state.toByteArray
            ))
          case Left(e) ⇒
            throw FileErrors.LocationInvalid
        }
      }
      db.run(action)
    }

  override def doHandleRemoveAvatar(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        UserCommands.UpdateAvatarAck(_, SeqState(seq, state)) ← DBIO.from(userExt.updateAvatar(client.userId, None))
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def doHandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← userExt.changeName(client.userId, name)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleEditNickName(nickname: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← userExt.changeNickname(client.userId, nickname)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleCheckNickName(nickname: String, clientData: ClientData): Future[HandlerResult[ResponseBool]] =
    authorized(clientData) { implicit client ⇒
      (for {
        _ ← fromBoolean(ProfileRpcErrors.NicknameInvalid)(StringUtils.validUsername(nickname))
        exists ← fromFuture(db.run(UserRepo.nicknameExists(nickname.trim)))
      } yield ResponseBool(!exists)).run
    }

  //todo: move validation inside of UserOffice
  override def doHandleEditAbout(about: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    authorized(clientData) { implicit client ⇒
      (for {
        trimmed ← point(about.map(_.trim))
        _ ← fromBoolean(ProfileRpcErrors.AboutTooLong)(trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true))
        SeqState(seq, state) ← fromFuture(userExt.changeAbout(client.userId, trimmed))
      } yield ResponseSeq(seq, state.toByteArray)).run
    }
  }

  override def doHandleEditMyTimeZone(tz: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      (for {
        SeqState(seq, state) ← fromFuture(produceError)(userExt.changeTimeZone(client.userId, tz))
      } yield ResponseSeq(seq, state.toByteArray)).run
    }

  override def doHandleEditMyPreferredLanguages(preferredLanguages: IndexedSeq[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      (for {
        SeqState(seq, state) ← fromFuture(produceError)(userExt.changePreferredLanguages(client.userId, preferredLanguages))
      } yield ResponseSeq(seq, state.toByteArray)).run
    }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case FileErrors.LocationInvalid ⇒ FileRpcErrors.LocationInvalid
    case UserErrors.InvalidName     ⇒ ProfileRpcErrors.NameInvalid
    case UserErrors.NicknameTaken   ⇒ ProfileRpcErrors.NicknameBusy
    case UserErrors.InvalidNickname ⇒ ProfileRpcErrors.NicknameInvalid
  }

  private def produceError = PartialFunction[Throwable, RpcError] {
    case SequenceErrors.UpdateAlreadyApplied(field) ⇒ RpcError(400, "UPDATE_ALREADY_APPLIED", s"$field already updated.", canTryAgain = false, data = None)
    case UserErrors.InvalidLocale(locale) ⇒ RpcError(400, "INVALID_LOCALE", s"Invalid language: $locale.", canTryAgain = false, data = None)
    case UserErrors.InvalidTimeZone(tz) ⇒ RpcError(400, "INVALID_TIME_ZONE", s"Invalid time zone: $tz.", canTryAgain = false, data = None)
    case UserErrors.EmptyLocalesList ⇒ RpcError(400, "EMPTY_LOCALES_LIST", s"Empty languages list.", canTryAgain = false, data = None)
    case e ⇒ throw e
  }
}