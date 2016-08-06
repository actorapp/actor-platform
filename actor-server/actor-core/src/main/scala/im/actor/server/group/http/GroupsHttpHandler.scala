package im.actor.server.group.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.util.FastFuture
import com.github.ghik.silencer.silent
import im.actor.server.api.http.json.JsonFormatters.{ errorsFormat, groupInviteInfoFormat }
import im.actor.server.api.http.{ HttpHandler, json }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ Avatar, FileLocation, FileStorageExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.persist._
import im.actor.server.persist.files.FileRepo
import im.actor.server.user.UserExtension
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.util.{ Failure, Success }

private[group] final class GroupsHttpHandler()(implicit system: ActorSystem) extends HttpHandler {

  import im.actor.server.ApiConversions._
  import system.dispatcher
  private val db = DbExtension(system).db
  private val fsAdapter = FileStorageExtension(system).fsAdapter
  private val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  override def routes: Route =
    defaultVersion {
      path("groups" / "invites" / Segment) { tokenOrShortName ⇒
        get {
          onComplete(retrieve(tokenOrShortName)) {
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

  private def retrieve(tokenOrShortName: String): Future[Either[json.Errors, json.GroupInviteInfo]] = for {
    byToken ← db.run(GroupInviteTokenRepo.findByToken(tokenOrShortName): @silent)
    byGroupId ← globalNamesStorage.getGroupId(tokenOrShortName)
    optInviteData = (byToken, byGroupId) match {
      case (Some(tokenInfo), _) ⇒ Some(tokenInfo.groupId → Some(tokenInfo.creatorId))
      case (_, Some(groupId))   ⇒ Some(groupId → None)
      case _                    ⇒ None
    }
    result ← optInviteData map {
      case (groupId, optInviterId) ⇒
        for {
          groupInfo ← GroupExtension(system).getApiStruct(groupId, 0)
          isPublic ← GroupExtension(system).getApiFullStruct(groupId, 0) map (_.shortName.isDefined)
          groupTitle = groupInfo.title
          groupAvatarUrls ← avatarUrls(groupInfo.avatar)

          optInviterInfo ← optInviterId match {
            case Some(inviterId) ⇒
              for {
                user ← UserExtension(system).getApiStruct(inviterId, 0, 0L)
                avatars ← avatarUrls(user.avatar)
              } yield Some(json.InviterInfo(user.name, avatars))
            case None ⇒ FastFuture.successful(None)
          }
        } yield Right(
          json.GroupInviteInfo(
            group = json.GroupInfo(groupId, groupTitle, isPublic, groupAvatarUrls),
            inviter = optInviterInfo
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
