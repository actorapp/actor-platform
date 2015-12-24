package im.actor.server.api.http.groups

import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileStorageExtension, FileLocation, ImageUtils }

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json.JsonFormatters.{ errorsFormat, groupInviteInfoFormat }
import im.actor.server.api.http.json.{ AvatarUrls, Errors, Group, GroupInviteInfo, User }
import ImageUtils.getAvatar
import im.actor.server.{ model, persist }

class GroupsHandler()(implicit system: ActorSystem) extends RoutesHandler {

  import system.dispatcher
  private val db = DbExtension(system).db
  private lazy val fsAdapter = FileStorageExtension(system).fsAdapter

  override def routes: Route = path("groups" / "invites" / Segment) { token ⇒
    get {
      onComplete(retrieve(token)) {
        case Success(Right(result)) ⇒
          complete(HttpResponse(
            status = OK,
            entity = Json.stringify(Json.toJson(result))
          ))
        case Success(Left(errors)) ⇒
          complete(HttpResponse(
            status = NotAcceptable,
            entity = Json.stringify(Json.toJson(errors))
          ))
        case Failure(e) ⇒ complete(HttpResponse(InternalServerError))
      }
    }
  }

  private def retrieve(token: String): Future[Either[Errors, GroupInviteInfo]] =
    db.run {
      for {
        optToken ← persist.GroupInviteTokenRepo.findByToken(token)
        result ← optToken.map { token ⇒
          for {
            groupTitle ← persist.GroupRepo.findTitle(token.groupId)
            groupAvatar ← persist.AvatarDataRepo.findByGroupId(token.groupId)
            groupAvatarUrls ← avatarUrls(groupAvatar)

            inviterName ← persist.UserRepo.findName(token.creatorId)
            inviterAvatar ← persist.AvatarDataRepo.findByUserId(token.creatorId).headOption
            inviterAvatarUrls ← avatarUrls(inviterAvatar)
          } yield Right(GroupInviteInfo(group = Group(groupTitle.getOrElse("Group"), groupAvatarUrls), inviter = User(inviterName.getOrElse("User"), inviterAvatarUrls)))
        }.getOrElse(DBIO.successful(Left(Errors("Expired or invalid token"))))
      } yield result
    }

  private def avatarUrls(optAvatar: Option[model.AvatarData]): DBIO[Option[AvatarUrls]] = {
    optAvatar.map(getAvatar).map { avatar ⇒
      for {
        small ← avatar.smallImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
        large ← avatar.largeImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
        full ← avatar.fullImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
      } yield Some(AvatarUrls(small, large, full))
    }.getOrElse(DBIO.successful(None))
  }

  private def urlOrNone(location: FileLocation): DBIO[Option[String]] = {
    implicit val timeout = 1.day
    for {
      fileOpt ← persist.FileRepo.find(location.fileId)
      url ← fileOpt.map { file ⇒
        DBIO.from(fsAdapter.getFileDownloadUrl(file, location.accessHash))
      }.getOrElse(DBIO.successful(None))
    } yield url
  }

}