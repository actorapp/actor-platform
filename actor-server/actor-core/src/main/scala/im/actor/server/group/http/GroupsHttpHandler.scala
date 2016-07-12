package im.actor.server.group.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.util.FastFuture
import im.actor.server.api.http.{ HttpHandler, json }
import im.actor.server.api.http.json.JsonFormatters.{ errorsFormat, groupInviteInfoFormat }
import im.actor.server.db.DbExtension
import im.actor.server.file.ImageUtils.getAvatar
import im.actor.server.file.{ Avatar, FileLocation, FileStorageExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.model.AvatarData
import im.actor.server.persist._
import im.actor.server.persist.files.FileRepo
import im.actor.server.user.UserExtension
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

private[group] final class GroupsHttpHandler()(implicit system: ActorSystem) extends HttpHandler {

  import im.actor.server.ApiConversions._

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

  private def retrieve(token: String): Future[Either[json.Errors, json.GroupInviteInfo]] = for {
    optTokenInfo ← db.run(GroupInviteTokenRepo.findByToken(token))
    result ← optTokenInfo map { tokenInfo ⇒
      for {
        groupInfo ← GroupExtension(system).getApiStruct(tokenInfo.groupId, 0)
        groupTitle = groupInfo.title
        groupAvatar = groupInfo.avatar
        groupAvatarUrls ← avatarUrls(groupAvatar)

        inviterInfo ← UserExtension(system).getApiStruct(tokenInfo.creatorId, 0, 0L)
        inviterName = inviterInfo.name
        inviterAvatar = inviterInfo.avatar
        inviterAvatarUrls ← avatarUrls(inviterAvatar)
      } yield Right(
        json.GroupInviteInfo(
          group = json.Group(groupTitle, groupAvatarUrls),
          inviter = json.User(inviterName, inviterAvatarUrls)
        )
      )
    } getOrElse FastFuture.successful(Left(json.Errors("Expired or invalid token")))
  } yield result

  private def avatarUrls(optAvatar: Option[Avatar]): Future[Option[json.AvatarUrls]] = {
    optAvatar map { avatar ⇒
      for {
        small ← avatar.smallImage.map(i ⇒ urlOrNone(i.fileLocation)) getOrElse FastFuture.successful(None)
        large ← avatar.largeImage.map(i ⇒ urlOrNone(i.fileLocation)) getOrElse FastFuture.successful(None)
        full ← avatar.fullImage.map(i ⇒ urlOrNone(i.fileLocation)) getOrElse FastFuture.successful(None)
      } yield Some(json.AvatarUrls(small, large, full))
    } getOrElse FastFuture.successful(Some(json.AvatarUrls(None, None, None)))
  }

  private def urlOrNone(location: FileLocation): Future[Option[String]] =
    for {
      fileOpt ← db.run(FileRepo.find(location.fileId))
      url ← fileOpt map { file ⇒
        fsAdapter.getFileDownloadUrl(file, location.accessHash)
      } getOrElse FastFuture.successful(None)
    } yield url

}
