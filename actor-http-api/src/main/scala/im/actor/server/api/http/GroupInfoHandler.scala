package im.actor.server.api.http

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import slick.driver.PostgresDriver.api._

import im.actor.server.util.FileUtils.getFileUrl
import im.actor.server.util.ImageUtils.getAvatar
import im.actor.server.{ models, persist }

class GroupInfoHandler(s3BucketName: String)(
  implicit
  db:     Database,
  system: ActorSystem,
  ec:     ExecutionContext,
  client: AmazonS3ScalaClient
) {

  def retrieve(token: String): Future[Either[Errors, GroupInviteInfo]] =
    db.run {
      for {
        optToken ← persist.GroupInviteToken.findByToken(token)
        result ← optToken.map { token ⇒
          for {
            groupTitle ← persist.Group.findTitle(token.groupId)
            groupAvatar ← persist.AvatarData.findByGroupId(token.groupId)
            groupAvatarUrl ← avatarUrl(groupAvatar)

            inviterName ← persist.User.findName(token.creatorId)
            inviterAvatar ← persist.AvatarData.findByUserId(token.creatorId).headOption
            inviterAvatarUrl ← avatarUrl(inviterAvatar)
          } yield Right(GroupInviteInfo(groupTitle.getOrElse("Group"), groupAvatarUrl, inviterName.getOrElse("User"), inviterAvatarUrl))
        }.getOrElse(DBIO.successful(Left(Errors("Expired or invalid token"))))
      } yield result
    }

  private def avatarUrl(optAvatar: Option[models.AvatarData]): DBIO[Option[String]] = {
    val optLocation = for {
      modelAvatar ← optAvatar
      structAvatar = getAvatar(modelAvatar)
      fileLocation ← List(structAvatar.largeImage, structAvatar.smallImage, structAvatar.fullImage).find(_.isDefined).flatten.map(_.fileLocation)
    } yield fileLocation
    implicit val timeout = 1.day
    optLocation.map { location ⇒
      for {
        fileOpt ← persist.File.find(location.fileId)
        url ← fileOpt.map { file ⇒
          DBIO.from(getFileUrl(file, location.accessHash, s3BucketName))
        }.getOrElse(DBIO.successful(None))
      } yield url
    }.getOrElse(DBIO.successful(None))
  }

}