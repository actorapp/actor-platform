package im.actor.server.api.rpc.service.profile

import im.actor.server.user.UserCommands.ChangeNameAck
import im.actor.server.user.{ UserOfficeRegion, UserOffice }

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import com.amazonaws.services.s3.transfer.TransferManager
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.misc.{ ResponseBool, ResponseSeq }
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import im.actor.api.rpc.users.{ UpdateUserAboutChanged, UpdateUserNickChanged, UpdateUserAvatarChanged, UpdateUserNameChanged }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ StringUtils, FileStorageAdapter, ImageUtils }
import im.actor.server.{ models, persist }
import im.actor.api.rpc.DBIOResult._

object ProfileErrors {
  val NicknameInvalid = RpcError(400, "NICK_NAME_INVALID",
    "Invalid nick name. Valid nick name should contain from 5 to 32 characters, and may consist of latin characters, numbers and underscores", false, None)
  val NicknameBusy = RpcError(400, "NICK_NAME_Busy", "This nickname already belongs some other user, we are sorry!", false, None)
  val AboutTooLong = RpcError(400, "ABOUT_TOO_LONG",
    "About is too long. It should be no longer then 255 characters", false, None)
}

class ProfileServiceImpl()(
  implicit
  actorSystem:         ActorSystem,
  db:                  Database,
  socialManagerRegion: SocialManagerRegion,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  fsAdapter:           FileStorageAdapter,
  userOffice:          UserOfficeRegion
) extends ProfileService {

  import ImageUtils._
  import FileHelpers._
  import SeqUpdatesManager._
  import SocialManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  implicit val timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleEditAvatar(fileLocation: FileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditAvatar]] = {
    // TODO: flatten

    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withFileLocation(fileLocation, AvatarSizeLimit) {
        scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current()) flatMap {
          case Right(avatar) ⇒
            val avatarData = getAvatarData(models.AvatarData.OfUser, client.userId, avatar)

            val update = UpdateUserAvatarChanged(client.userId, Some(avatar))

            for {
              _ ← persist.AvatarData.createOrUpdate(avatarData)
              relatedUserIds ← DBIO.from(getRelations(client.userId))
              _ ← broadcastClientAndUsersUpdate(relatedUserIds, update, None)
              seqstate ← broadcastClientUpdate(update, None)
            } yield {
              Ok(ResponseEditAvatar(avatar, seqstate.seq, seqstate.state.toByteArray))
            }
          case Left(e) ⇒
            actorSystem.log.error(e, "Failed to scale profile avatar")
            DBIO.successful(Error(Errors.LocationInvalid))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleRemoveAvatar(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val update = UpdateUserAvatarChanged(client.userId, None)

      for {
        _ ← persist.AvatarData.createOrUpdate(models.AvatarData.empty(models.AvatarData.OfUser, client.userId.toLong))
        relatedUserIds ← DBIO.from(getRelations(client.userId))
        _ ← broadcastClientAndUsersUpdate(relatedUserIds, update, None)
        seqstate ← broadcastClientUpdate(update, None)
      } yield Ok(ResponseSeq(seqstate.seq, seqstate.state.toByteArray))
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val future = UserOffice.changeName(client.userId, name) map {
        case ChangeNameAck(seq, state) ⇒ Ok(ResponseSeq(seq, state.toByteArray))
      }
      DBIO.from(future)
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
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
        _ ← fromDBIO(persist.User.setNickname(client.userId, trimmed))
        relatedUserIds ← fromFuture(getRelations(client.userId))
        update = UpdateUserNickChanged(client.userId, trimmed)
        (seqstate, _) ← fromDBIO(broadcastClientAndUsersUpdate(relatedUserIds, update, None))
      } yield ResponseSeq(seqstate._1, seqstate._2)
      action.run
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  def jhandleCheckNickName(nickname: String, clientData: ClientData): Future[HandlerResult[ResponseBool]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      (for {
        _ ← fromBoolean(ProfileErrors.NicknameInvalid)(StringUtils.validNickName(nickname))
        exists ← fromDBIO(persist.User.nicknameExists(nickname.trim))
      } yield ResponseBool(!exists)).run
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  def jhandleEditAbout(about: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val action: Result[ResponseSeq] = for {
        trimmed ← point(about.map(_.trim))
        _ ← fromBoolean(ProfileErrors.AboutTooLong)(trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true))
        _ ← fromDBIO(persist.User.setAbout(client.userId, trimmed))
        relatedUserIds ← fromFuture(getRelations(client.userId))
        update = UpdateUserAboutChanged(client.userId, trimmed)
        (seqstate, _) ← fromDBIO(broadcastClientAndUsersUpdate(relatedUserIds, update, None))
      } yield ResponseSeq(seqstate._1, seqstate._2)
      action.run
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }
}