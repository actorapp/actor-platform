package im.actor.server.api.http.groups

import im.actor.server.file.{ FileStorageAdapter, ImageUtils }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.ApiFileLocation
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json.JsonFormatters.{ errorsFormat, groupInviteInfoFormat }
import im.actor.server.api.http.json.{ AvatarUrls, Errors, Group, GroupInviteInfo, User }
import ImageUtils.getAvatar
import im.actor.server.{ models, persist }

class GroupsHandler()(
  implicit
  db:        Database,
  system:    ActorSystem,
  ec:        ExecutionContext,
  fsAdapter: FileStorageAdapter
) extends RoutesHandler {

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
        optToken ← persist.GroupInviteToken.findByToken(token)
        result ← optToken.map { token ⇒
          for {
            groupTitle ← persist.Group.findTitle(token.groupId)
            groupAvatar ← persist.AvatarData.findByGroupId(token.groupId)
            groupAvatarUrls ← avatarUrls(groupAvatar)

            inviterName ← persist.User.findName(token.creatorId)
            inviterAvatar ← persist.AvatarData.findByUserId(token.creatorId).headOption
            inviterAvatarUrls ← avatarUrls(inviterAvatar)
          } yield Right(GroupInviteInfo(group = Group(groupTitle.getOrElse("Group"), groupAvatarUrls), inviter = User(inviterName.getOrElse("User"), inviterAvatarUrls)))
        }.getOrElse(DBIO.successful(Left(Errors("Expired or invalid token"))))
      } yield result
    }

  private def avatarUrls(optAvatar: Option[models.AvatarData]): DBIO[Option[AvatarUrls]] = {
    optAvatar.map(getAvatar).map { avatar ⇒
      for {
        small ← avatar.smallImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None)) //TODO: rewrite with shapeless
        large ← avatar.largeImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
        full ← avatar.fullImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
      } yield Some(AvatarUrls(small, large, full))
    }.getOrElse(DBIO.successful(None))
  }

  private def urlOrNone(location: ApiFileLocation): DBIO[Option[String]] = {
    implicit val timeout = 1.day
    for {
      fileOpt ← persist.File.find(location.fileId)
      url ← fileOpt.map { file ⇒
        DBIO.from(fsAdapter.getFileUrl(file, location.accessHash))
      }.getOrElse(DBIO.successful(None))
    } yield url
  }

}