package im.actor.server.api.rpc.service.profile

import im.actor.server.{ ApiConversions, persist }

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import ApiConversions._
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileStorageAdapter, S3StorageExtension, ImageUtils, FileErrors }
import im.actor.server.persist
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sequence.SeqState
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user._
import im.actor.util.misc.StringUtils

object ProfileErrors {
  val NicknameInvalid = RpcError(400, "NICKNAME_INVALID",
    "Invalid nickname. Valid nickname should contain from 5 to 32 characters, and may consist of latin characters, numbers and underscores", false, None)
  val NicknameBusy = RpcError(400, "NICKNAME_BUSY", "This nickname already belongs some other user, we are sorry!", false, None)
  val AboutTooLong = RpcError(400, "ABOUT_TOO_LONG",
    "About is too long. It should be no longer then 255 characters", false, None)
}

class ProfileServiceImpl()(
  implicit
  actorSystem: ActorSystem
) extends ProfileService {

  import FileHelpers._
  import ImageUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val timeout = Timeout(5.seconds)
  // TODO: configurable
  private implicit val db: Database = DbExtension(actorSystem).db
  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  private implicit val userProcessorRegion: UserProcessorRegion = UserExtension(actorSystem).processorRegion
  private implicit val userViewRegion: UserViewRegion = UserExtension(actorSystem).viewRegion
  private implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region
  private implicit val fsAdapter: FileStorageAdapter = S3StorageExtension(actorSystem).s3StorageAdapter

  override def jhandleEditAvatar(fileLocation: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditAvatar]] = {
    // TODO: flatten

    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withFileLocation(fileLocation, AvatarSizeLimit) {
        scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current()) flatMap {
          case Right(avatar) ⇒
            for {
              UserCommands.UpdateAvatarAck(avatar, SeqState(seq, state)) ← DBIO.from(UserOffice.updateAvatar(client.userId, client.authId, Some(avatar)))
            } yield Ok(ResponseEditAvatar(
              avatar.get,
              seq,
              state.toByteArray
            ))
          case Left(e) ⇒
            throw FileErrors.LocationInvalid
        }
      }
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case FileErrors.LocationInvalid ⇒ Error(Errors.LocationInvalid)
    }
  }

  override def jhandleRemoveAvatar(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      for {
        UserCommands.UpdateAvatarAck(_, SeqState(seq, state)) ← DBIO.from(UserOffice.updateAvatar(client.userId, client.authId, None))
      } yield Ok(ResponseSeq(seq, state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      DBIO.from(UserOffice.changeName(client.userId, name) map {
        case SeqState(seq, state) ⇒ Ok(ResponseSeq(seq, state.toByteArray))
      })
    }
    db.run(toDBIOAction(authorizedAction))
  }

  def jhandleEditNickName(nickname: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val action: Result[ResponseSeq] = for {
        trimmed ← point(nickname.map(_.trim))
        _ ← fromBoolean(ProfileErrors.NicknameInvalid)(trimmed.map(StringUtils.validNickName).getOrElse(true))
        _ ← if (trimmed.isDefined) {
          for {
            checkExist ← fromOption(ProfileErrors.NicknameInvalid)(trimmed)
            _ ← fromDBIOBoolean(ProfileErrors.NicknameBusy)(persist.User.nicknameExists(checkExist).map(exist ⇒ !exist))
          } yield ()
        } else point(())
        SeqState(seq, state) ← fromFuture(UserOffice.changeNickname(client.userId, client.authId, trimmed))
      } yield ResponseSeq(seq, state.toByteArray)
      action.run
    }
    db.run(toDBIOAction(authorizedAction))
  }

  def jhandleCheckNickName(nickname: String, clientData: ClientData): Future[HandlerResult[ResponseBool]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      (for {
        _ ← fromBoolean(ProfileErrors.NicknameInvalid)(StringUtils.validNickName(nickname))
        exists ← fromDBIO(persist.User.nicknameExists(nickname.trim))
      } yield ResponseBool(!exists)).run
    }
    db.run(toDBIOAction(authorizedAction))
  }

  //todo: move validation inside of UserOffice
  def jhandleEditAbout(about: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val action: Result[ResponseSeq] = for {
        trimmed ← point(about.map(_.trim))
        _ ← fromBoolean(ProfileErrors.AboutTooLong)(trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true))

        SeqState(seq, state) ← fromFuture(UserOffice.changeAbout(client.userId, client.authId, trimmed))
      } yield ResponseSeq(seq, state.toByteArray)
      action.run
    }
    db.run(toDBIOAction(authorizedAction))
  }
}