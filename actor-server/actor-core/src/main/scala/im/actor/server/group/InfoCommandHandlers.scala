package im.actor.server.group

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import akka.http.scaladsl.util.FastFuture
import com.github.ghik.silencer.silent
import im.actor.api.rpc.files.ApiAvatar
import im.actor.api.rpc.groups._
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.group.GroupCommands.{ AddExt, AddExtAck, MakeHistoryShared, RemoveExt, RemoveExtAck, UpdateAbout, UpdateAvatar, UpdateAvatarAck, UpdateShortName, UpdateTitle, UpdateTopic }
import im.actor.server.group.GroupErrors._
import im.actor.server.group.GroupEvents.{ AboutUpdated, AvatarUpdated, ShortNameUpdated, TitleUpdated, TopicUpdated }
import im.actor.server.model.AvatarData
import im.actor.server.names.{ GlobalNameOwner, OwnerType }
import im.actor.server.persist.{ AvatarDataRepo, GroupRepo }
import im.actor.server.sequence.{ Optimization, SeqState, SeqStateDate }
import im.actor.util.misc.StringUtils

import scala.concurrent.Future

private[group] trait InfoCommandHandlers {
  this: GroupProcessor ⇒

  import im.actor.server.ApiConversions._

  protected def updateAvatar(cmd: UpdateAvatar): Unit = {
    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else {
      persist(AvatarUpdated(Instant.now, cmd.avatar)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val apiAvatar: Option[ApiAvatar] = cmd.avatar
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupAvatarChanged(groupId, apiAvatar)
        val updateObsolete = UpdateGroupAvatarChangedObsolete(groupId, cmd.clientUserId, apiAvatar, dateMillis, cmd.randomId)
        val serviceMessage = GroupServiceMessages.changedAvatar(apiAvatar)

        db.run(AvatarDataRepo.createOrUpdate(getAvatarData(cmd.avatar)))
        val result: Future[UpdateAvatarAck] = for {
          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(cmd.clientUserId, cmd.clientAuthId, memberIds, updateObsolete)

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield UpdateAvatarAck(apiAvatar).withSeqStateDate(SeqStateDate(seq, state, date))

        result pipeTo sender()
      }
    }
  }

  protected def updateTitle(cmd: UpdateTitle): Unit = {
    val title = cmd.title
    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidTitle(title)) {
      sender() ! Status.Failure(InvalidTitle)
    } else {
      persist(TitleUpdated(Instant.now(), title)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupTitleChanged(groupId, title)
        val updateObsolete = UpdateGroupTitleChangedObsolete(
          groupId,
          userId = cmd.clientUserId,
          title = title,
          date = dateMillis,
          randomId = cmd.randomId
        )
        val serviceMessage = GroupServiceMessages.changedTitle(title)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.titleChanged(newState.groupType)))

        //TODO: remove deprecated
        db.run(GroupRepo.updateTitle(groupId, title, cmd.clientUserId, cmd.randomId, date = evt.ts): @silent)

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield SeqStateDate(seq, state, date)

        result pipeTo sender()
      }

    }
  }

  //TODO: who can update topic???
  protected def updateTopic(cmd: UpdateTopic): Unit = {
    def isValidTopic(topic: Option[String]) = topic.forall(_.length < 255)

    val topic = trimToEmpty(cmd.topic)

    if (state.groupType.isChannel && !state.isAdmin(cmd.clientUserId)) {
      sender() ! notAdmin
    } else if (state.nonMember(cmd.clientUserId)) {
      sender() ! notMember
    } else if (!isValidTopic(topic)) {
      sender() ! Status.Failure(TopicTooLong)
    } else {
      persist(TopicUpdated(Instant.now, topic)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupTopicChanged(groupId, topic)
        val updateObsolete = UpdateGroupTopicChangedObsolete(
          groupId,
          randomId = cmd.randomId,
          userId = cmd.clientUserId,
          topic = topic,
          date = dateMillis
        )
        val serviceMessage = GroupServiceMessages.changedTopic(topic)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.topicChanged(newState.groupType)))

        //TODO: remove deprecated
        db.run(GroupRepo.updateTopic(groupId, topic): @silent)

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield SeqStateDate(seq, state, date)

        result pipeTo sender()
      }
    }
  }

  protected def updateAbout(cmd: UpdateAbout): Unit = {
    def isValidAbout(about: Option[String]) = about.forall(_.length < 255)

    val about = trimToEmpty(cmd.about)

    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidAbout(about)) {
      sender() ! Status.Failure(AboutTooLong)
    } else {

      persist(AboutUpdated(Instant.now, about)) { evt ⇒
        val newState = commit(evt)

        val memberIds = newState.memberIds

        val updateNew = UpdateGroupAboutChanged(groupId, about)
        val updateObsolete = UpdateGroupAboutChangedObsolete(groupId, about)
        val serviceMessage = GroupServiceMessages.changedAbout(about)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.topicChanged(newState.groupType)))

        //TODO: remove deprecated
        db.run(GroupRepo.updateAbout(groupId, about): @silent)

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
        } yield SeqStateDate(seq, state, evt.ts.toEpochMilli)

        result pipeTo sender()
      }
    }
  }

  protected def updateShortName(cmd: UpdateShortName): Unit = {
    def isValidShortName(shortName: Option[String]) = shortName forall StringUtils.validGlobalName

    val oldShortName = state.shortName
    val newShortName = trimToEmpty(cmd.shortName)

    if (!state.permissions.canEditShortName(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidShortName(newShortName)) {
      sender() ! Status.Failure(InvalidShortName)
    } else if (oldShortName == newShortName) {
      seqUpdExt.getSeqState(cmd.clientUserId, cmd.clientAuthId) pipeTo sender()
    } else {
      val replyTo = sender()

      val existsFu = newShortName map { name ⇒
        globalNamesStorage.exists(name)
      } getOrElse FastFuture.successful(false)

      //TODO: timeout for this
      onSuccess(existsFu) { exists ⇒
        if (exists) {
          replyTo ! Status.Failure(ShortNameTaken)
        } else {
          // when user sets short name first time - we making group history shared
          if (state.shortName.isEmpty && newShortName.nonEmpty && !state.isHistoryShared) {
            context.parent !
              GroupEnvelope(groupId)
              .withMakeHistoryShared(MakeHistoryShared(cmd.clientUserId, cmd.clientAuthId))
          }

          persist(ShortNameUpdated(Instant.now, newShortName)) { evt ⇒
            val newState = commit(evt)

            val memberIds = newState.memberIds

            val result: Future[SeqState] = for {
              _ ← globalNamesStorage.updateOrRemove(
                oldShortName,
                newShortName,
                GlobalNameOwner(OwnerType.Group, groupId)
              )
              seqState ← seqUpdExt.broadcastClientUpdate(
                userId = cmd.clientUserId,
                authId = cmd.clientAuthId,
                bcastUserIds = memberIds - cmd.clientUserId,
                update = UpdateGroupShortNameChanged(groupId, newShortName)
              )
            } yield seqState

            result pipeTo replyTo
          }
        }
      }
    }
  }

  protected def addExt(cmd: AddExt): Unit =
    cmd.ext match {
      case Some(ext) ⇒
        persist(GroupEvents.ExtAdded(Instant.now, ext)) { evt ⇒
          val newState = commit(evt)
          sendExtUpdate(newState) map (_ ⇒ AddExtAck()) pipeTo sender()
        }
      case None ⇒
        sender() ! Status.Failure(InvalidExtension)
    }

  protected def removeExt(cmd: RemoveExt): Unit =
    if (state.exts.exists(_.key == cmd.key)) {
      persist(GroupEvents.ExtRemoved(Instant.now, cmd.key)) { evt ⇒
        val newState = commit(evt)
        sendExtUpdate(newState) map (_ ⇒ RemoveExtAck()) pipeTo sender()
      }
    } else {
      sender() ! RemoveExtAck()
    }

  private def sendExtUpdate(state: GroupState): Future[Unit] =
    seqUpdExt.broadcastPeopleUpdate(
      userIds = state.memberIds,
      update = UpdateGroupExtChanged(groupId, Some(extToApi(state.exts)))
    )

  private def getAvatarData(avatar: Option[Avatar]): AvatarData =
    avatar
      .map(ImageUtils.getAvatarData(AvatarData.OfGroup, groupId, _))
      .getOrElse(AvatarData.empty(AvatarData.OfGroup, groupId.toLong))

  private def trimToEmpty(s: Option[String]): Option[String] =
    s map (_.trim) filter (_.nonEmpty)

}
