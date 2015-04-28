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
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseVoid }
import im.actor.api.rpc.profile.{ ProfileService, ResponseEditAvatar }
import im.actor.api.rpc.users.{ UpdateUserAvatarChanged, UpdateUserNameChanged }
import im.actor.server.api.util.{ FileUtils, ACL, AvatarUtils }
import im.actor.server.models
import im.actor.server.persist
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }

class ProfileServiceImpl(bucketName: String)(
  implicit
  transferManager:     TransferManager,
  db:                  Database,
  socialManagerRegion: SocialManagerRegion,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  actorSystem:         ActorSystem
) extends ProfileService {

  import AvatarUtils._
  import FileUtils._
  import SeqUpdatesManager._
  import SocialManager._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  implicit val timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleEditAvatar(fileLocation: FileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditAvatar]] = {
    // TODO: flatten

    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withFileLocation(fileLocation, AvatarSizeLimit) {
        scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current(), bucketName) flatMap {
          case Some(avatar) ⇒
            val avatarData = getAvatarData(models.AvatarData.OfUser, client.userId, avatar)

            val update = UpdateUserAvatarChanged(client.userId, Some(avatar))

            for {
              _ ← persist.AvatarData.createOrUpdate(avatarData)
              relatedUserIds ← DBIO.from(getRelations(client.userId))
              _ ← broadcastUpdateAll(relatedUserIds, update, None)
              seqstate ← broadcastClientUpdate(update, None)
            } yield {
              Ok(ResponseEditAvatar(avatar, seqstate._1, seqstate._2))
            }
          case None ⇒
            DBIO.successful(Error(Errors.LocationInvalid))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleRemoveAvatar(clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    Future {
      throw new Exception("Not implemented")
    }
  }

  override def jhandleDetachEmail(email: Int, accessHash: Long, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    Future {
      throw new Exception("Not implemented")
    }
  }

  override def jhandleSendEmailCode(email: String, description: Option[String], clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    Future {
      throw new Exception("Not implemented")
    }
  }

  override def jhandleChangeEmailTitle(emailId: Int, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    Future {
      throw new Exception("Not implemented")
    }
  }

  override def jhandleChangePhoneTitle(phoneId: Int, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    Future {
      throw new Exception("Not implemented")
    }
  }

  override def jhandleEditName(name: String, clientData: ClientData): Future[HandlerResult[ResponseSeq]] = {
    val authorizedAction = requireAuth(clientData) map { implicit client ⇒
      val update = UpdateUserNameChanged(client.userId, name)

      for {
        _ ← persist.User.setName(client.userId, name)
        relatedUserIds ← DBIO.from(getRelations(client.userId))
        (seqstate, _) ← broadcastUpdateAll(relatedUserIds, update, None)
      } yield Ok(ResponseSeq(seqstate._1, seqstate._2))
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }
}