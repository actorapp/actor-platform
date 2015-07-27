package im.actor.server.group

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor.{ Status, ActorRef }
import akka.pattern.pipe
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups.UpdateGroupAvatarChanged
import im.actor.server.api.ApiConversions._
import im.actor.server.file.{ FileErrors, FileLocation }
import im.actor.server.group.GroupEvents.AvatarUpdated
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.util.ImageUtils._
import im.actor.server.util.{ FileStorageAdapter, GroupServiceMessages, HistoryUtils }
import im.actor.server.{ models, persist ⇒ p }

private[group] trait GroupCommandHandlers {
  this: GroupOfficeActor ⇒

  import GroupCommands._

  private implicit val system = context.system
  private implicit val ec = context.dispatcher

  protected def updateAvatar(group: Group, clientUserId: Int, clientAuthId: Long, fileLocationOpt: Option[FileLocation], randomId: Long)(
    implicit
    fsAdapter:           FileStorageAdapter,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Unit = {
    val replyTo = sender()

    val avatarFuture = fileLocationOpt match {
      case Some(fileLocation) ⇒
        db.run(scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current())) map {
          case Right(avatar) ⇒
            Some(avatar)
          case Left(e) ⇒
            log.error(e, "Failed to scale group avatar")
            throw FileErrors.LocationInvalid
        }
      case None ⇒
        Future.successful(None)
    }

    avatarFuture foreach { avatarOpt ⇒
      val date = new DateTime
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfGroup, groupId, _)) getOrElse (models.AvatarData.empty(models.AvatarData.OfGroup, groupId.toLong))

      persistStashingReply(AvatarUpdated(avatarOpt), replyTo)(workWith(_, group)) { evt ⇒
        val update = UpdateGroupAvatarChanged(groupId, clientUserId, avatarOpt, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.changedAvatar(avatarOpt)

        db.run(for {
          _ ← p.AvatarData.createOrUpdate(avatarData)
          groupUserIds ← p.GroupUser.findUserIds(groupId)
          (seqstate, _) ← broadcastClientAndUsersUpdate(clientUserId, clientAuthId, groupUserIds.toSet, update, None, isFat = false)
        } yield {
          db.run(HistoryUtils.writeHistoryMessage(
            models.Peer.privat(clientUserId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          ))

          UpdateAvatarResponse(avatarOpt, SeqStateDate(seqstate.seq, seqstate.state, date.getMillis))
        })
      }
    }
  }
}
