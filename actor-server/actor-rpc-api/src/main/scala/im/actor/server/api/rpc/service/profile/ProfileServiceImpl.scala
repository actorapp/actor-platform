package im.actor.server.api.rpc.service.profile

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import com.amazonaws.services.s3.transfer.TransferManager
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.misc.ResponseSeq
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import im.actor.api.rpc.users.{ UpdateUserAvatarChanged, UpdateUserNameChanged }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ FileStorageAdapter, ImageUtils }
import im.actor.server.{ models, persist }

class ProfileServiceImpl(bucketName: String)(
  implicit
  fsAdapter:           FileStorageAdapter,
  transferManager:     TransferManager,
  db:                  Database,
  socialManagerRegion: SocialManagerRegion,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  actorSystem:         ActorSystem
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
        scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current(), bucketName) flatMap {
          case Right(avatar) ⇒
            val avatarData = getAvatarData(models.AvatarData.OfUser, client.userId, avatar)

            val update = UpdateUserAvatarChanged(client.userId, Some(avatar))

            for {
              _ ← persist.AvatarData.createOrUpdate(avatarData)
              relatedUserIds ← DBIO.from(getRelations(client.userId))
              _ ← broadcastClientAndUsersUpdate(relatedUserIds, update, None)
              seqstate ← broadcastClientUpdate(update, None)
            } yield {
              Ok(ResponseEditAvatar(avatar, seqstate._1, seqstate._2))
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
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val update = UpdateUserNameChanged(client.userId, name)

      for {
        _ ← persist.User.setName(client.userId, name)
        relatedUserIds ← DBIO.from(getRelations(client.userId))
        (seqstate, _) ← broadcastClientAndUsersUpdate(relatedUserIds, update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }
}