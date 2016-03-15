package im.actor.server.group.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.actor.server.api.http.{ HttpHandler, json }
import im.actor.server.api.http.json.JsonFormatters.{ errorsFormat, groupInviteInfoFormat }
import im.actor.server.db.DbExtension
import im.actor.server.file.ImageUtils.getAvatar
import im.actor.server.file.{ FileLocation, FileStorageExtension }
import im.actor.server.model.AvatarData
import im.actor.server.persist._
import im.actor.server.persist.files.FileRepo
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

private[group] final class GroupsHttpHandler()(implicit system: ActorSystem) extends HttpHandler {

  import system.dispatcher
  private val db = DbExtension(system).db
  private val fsAdapter = FileStorageExtension(system).fsAdapter

  override def routes: Route =
    defaultVersion {
      path("groups" / "invites" / Segment) { token ⇒
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
    }

  private def retrieve(token: String): Future[Either[json.Errors, json.GroupInviteInfo]] =
    db.run {
      for {
        optToken ← GroupInviteTokenRepo.findByToken(token)
        result ← optToken.map { token ⇒
          for {
            groupTitle ← GroupRepo.findTitle(token.groupId)
            groupAvatar ← AvatarDataRepo.findByGroupId(token.groupId)
            groupAvatarUrls ← avatarUrls(groupAvatar)

            inviterName ← UserRepo.findName(token.creatorId)
            inviterAvatar ← AvatarDataRepo.findByUserId(token.creatorId).headOption
            inviterAvatarUrls ← avatarUrls(inviterAvatar)
          } yield Right(json.GroupInviteInfo(group = json.Group(groupTitle.getOrElse("Group"), groupAvatarUrls), inviter = json.User(inviterName.getOrElse("User"), inviterAvatarUrls)))
        }.getOrElse(DBIO.successful(Left(json.Errors("Expired or invalid token"))))
      } yield result
    }

  private def avatarUrls(optAvatar: Option[AvatarData]): DBIO[Option[json.AvatarUrls]] = {
    optAvatar.map(getAvatar).map { avatar ⇒
      for {
        small ← avatar.smallImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
        large ← avatar.largeImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
        full ← avatar.fullImage.map(i ⇒ urlOrNone(i.fileLocation)).getOrElse(DBIO.successful(None))
      } yield Some(json.AvatarUrls(small, large, full))
    }.getOrElse(DBIO.successful(None))
  }

  private def urlOrNone(location: FileLocation): DBIO[Option[String]] = {
    implicit val timeout = 1.day
    for {
      fileOpt ← FileRepo.find(location.fileId)
      url ← fileOpt.map { file ⇒
        DBIO.from(fsAdapter.getFileDownloadUrl(file, location.accessHash))
      }.getOrElse(DBIO.successful(None))
    } yield url
  }

}