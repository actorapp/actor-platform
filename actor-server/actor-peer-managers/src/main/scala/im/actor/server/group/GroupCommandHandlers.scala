package im.actor.server.group

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups.{ UpdateGroupTitleChanged, UpdateGroupAvatarChanged }
import im.actor.server.api.ApiConversions._
import im.actor.server.file.Avatar
import im.actor.server.office.PushTexts
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.util.ImageUtils._
import im.actor.server.util.{ FileStorageAdapter, GroupServiceMessages, HistoryUtils }
import im.actor.server.{ persist ⇒ p, models }

private[group] trait GroupCommandHandlers {
  self: GroupOfficeActor ⇒

  import GroupCommands._
  import GroupEvents._

  private implicit val system = context.system
  private implicit val ec = context.dispatcher

  protected def updateAvatar(group: Group, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long)(
    implicit
    fsAdapter:           FileStorageAdapter,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Unit = {
    persistStashingReply(AvatarUpdated(avatarOpt))(workWith(_, group)) { evt ⇒
      val date = new DateTime
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfGroup, groupId, _)) getOrElse (models.AvatarData.empty(models.AvatarData.OfGroup, groupId.toLong))

      val update = UpdateGroupAvatarChanged(groupId, clientUserId, avatarOpt, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.changedAvatar(avatarOpt)

      val memberIds = group.members.keySet

      db.run(for {
        _ ← p.AvatarData.createOrUpdate(avatarData)
        (seqstate, _) ← broadcastClientAndUsersUpdate(clientUserId, clientAuthId, memberIds, update, None, isFat = false)
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

  protected def makePublic(group: Group, description: String)(implicit db: Database): Unit = {
    persistStashingReply(Vector(BecamePublic(), DescriptionUpdated(description)))(workWith(_, group)) { _ ⇒
      db.run(DBIO.sequence(Seq(
        p.Group.makePublic(groupId),
        p.Group.updateDescription(groupId, description)
      ))) map (_ ⇒ MakePublicAck())
    }
  }
  protected def updateTitle(group: Group, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long)(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Unit = {
    val memberIds = group.members.keySet

    persistStashingReply(TitleUpdated(title))(workWith(_, group)) { _ ⇒
      val date = new DateTime

      val update = UpdateGroupTitleChanged(groupId = groupId, userId = clientUserId, title = title, date = date.getMillis, randomId = randomId)
      val serviceMessage = GroupServiceMessages.changedTitle(title)

      db.run(for {
        _ ← p.Group.updateTitle(groupId, title, clientUserId, randomId, date)
        _ ← HistoryUtils.writeHistoryMessage(
          models.Peer.privat(clientUserId),
          models.Peer.group(groupId),
          date,
          randomId,
          serviceMessage.header,
          serviceMessage.toByteArray
        )
        (seqstate, _) ← broadcastClientAndUsersUpdate(clientUserId, clientAuthId, memberIds, update, Some(PushTexts.TitleChanged), isFat = false)
      } yield SeqStateDate(seqstate.seq, seqstate.state, date.getMillis))
    }
  }
}
