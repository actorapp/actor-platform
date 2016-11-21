package im.actor.server.api.rpc.service.profile

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileErrors, FileStorageAdapter, FileStorageExtension, ImageUtils }
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.sequence.{ SeqState, SequenceErrors }
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user._
import im.actor.util.misc.StringUtils
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
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

  import FutureResultRpc._

  override implicit val ec: ExecutionContext = system.dispatcher

  private implicit val timeout = Timeout(5.seconds)
  // TODO: configurable
  private val db: Database = DbExtension(system).db
  private val userExt = UserExtension(system)
  private implicit val socialRegion: SocialManagerRegion = SocialExtension(system).region
  private implicit val fsAdapter: FileStorageAdapter = FileStorageExtension(system).fsAdapter
  private val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  // TODO: flatten
  override def doHandleEditAvatar(fileLocation: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditAvatar]] =
    authorized(clientData) { implicit client ⇒
      val action = withFileLocation(fileLocation, AvatarSizeLimit) {
        scaleAvatar(fileLocation.fileId) flatMap {
          case Right(avatar) ⇒
            for {
              UserCommands.UpdateAvatarAck(avatar, SeqState(seq, state)) ← DBIO.from(userExt.updateAvatar(client.userId, client.authId, Some(avatar)))
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

  override def doHandleRemoveAvatar(clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      val action = for {
        UserCommands.UpdateAvatarAck(_, SeqState(seq, state)) ← DBIO.from(userExt.updateAvatar(client.userId, client.authId, None))
      } yield Ok(ResponseSeq(seq, state.toByteArray))
      db.run(action)
    }

  override def doHandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← userExt.changeName(client.userId, client.authId, name)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleEditNickName(nickname: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      for {
        SeqState(seq, state) ← userExt.changeNickname(client.userId, client.authId, nickname)
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

  override def doHandleCheckNickName(nickname: String, clientData: ClientData): Future[HandlerResult[ResponseBool]] =
    authorized(clientData) { implicit client ⇒
      (for {
        _ ← fromBoolean(ProfileRpcErrors.NicknameInvalid)(StringUtils.validGlobalName(nickname))
        exists ← fromFuture(globalNamesStorage.exists(nickname.trim))
      } yield ResponseBool(!exists)).value
    }

  //todo: move validation inside of UserProcessor
  override def doHandleEditAbout(about: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    authorized(clientData) { implicit client ⇒
      (for {
        trimmed ← point(about.map(_.trim))
        _ ← fromBoolean(ProfileRpcErrors.AboutTooLong)(trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true))
        s ← fromFuture(userExt.changeAbout(client.userId, client.authId, trimmed))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }
  }

  override def doHandleEditMyTimeZone(tz: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      (for {
        s ← fromFuture(produceError)(userExt.changeTimeZone(client.userId, client.authId, tz))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
    }

  override def doHandleEditMyPreferredLanguages(preferredLanguages: IndexedSeq[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      (for {
        s ← fromFuture(produceError)(userExt.changePreferredLanguages(client.userId, client.authId, preferredLanguages))
      } yield ResponseSeq(s.seq, s.state.toByteArray)).value
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
