package im.actor.server.bot

import akka.actor.ActorSystem
import im.actor.api.rpc.Update
import im.actor.api.rpc.files.{ ApiFileLocation, ApiAvatarImage, ApiAvatar }
import im.actor.api.rpc.groups.{ ApiMember, ApiGroup }
import im.actor.api.rpc.messaging.{ ApiJsonMessage, UpdateMessage, ApiTextMessage }
import im.actor.api.rpc.users.ApiUser
import im.actor.bots.BotMessages._
import im.actor.server.acl.ACLUtils
import im.actor.server.group.GroupUtils
import im.actor.server.sequence.{ UpdateRefs, SeqUpdatesManager }
import im.actor.server.user.UserExtension

import scala.concurrent.Future
import scala.language.postfixOps

final class BotUpdateBuilder(botUserId: Int, botAuthId: Long, system: ActorSystem) {
  import system.dispatcher

  implicit val _system = system
  val userExt = UserExtension(system)

  def apply(seq: Int, upd: Update): Future[Option[BotFatSeqUpdate]] = {
    val updateOptFuture = upd match {
      case update: UpdateMessage ⇒
        (update.message match {
          case ApiTextMessage(text, _, _) ⇒ Some(TextMessage(text))
          case ApiJsonMessage(rawJson)    ⇒ Some(JsonMessage(rawJson))
          case _                          ⇒ None
        }) match {
          case Some(message) ⇒
            if (update.senderUserId != botUserId) {
              for {
                apiOutPeer ← ACLUtils.getOutPeer(update.peer, botAuthId)
                senderAccessHash ← userExt.getAccessHash(update.senderUserId, botAuthId)
              } yield Some(Message(
                peer = OutPeer(apiOutPeer.`type`.id, apiOutPeer.id, apiOutPeer.accessHash),
                sender = UserOutPeer(update.senderUserId, senderAccessHash),
                date = update.date,
                randomId = update.randomId,
                message = message
              ))
            } else
              Future.successful(None)
          case None ⇒ Future.successful(None)
        }
      case _ ⇒ Future.successful(None)
    }

    updateOptFuture flatMap {
      case Some(body) ⇒
        val UpdateRefs(userIds, groupIds) = SeqUpdatesManager.updateRefs(upd)

        for {
          (apiGroups, apiUsers) ← GroupUtils.getGroupsUsers(groupIds, userIds, botUserId, botAuthId)
        } yield Some(BotFatSeqUpdate(
          seq = seq,
          body = body,
          users = buildUsers(apiUsers),
          groups = buildGroups(apiGroups)
        ))
      case None ⇒ Future.successful(None)
    }
  }

  private def buildGroups(apiGroups: Seq[ApiGroup]): Map[Int, Group] = {
    apiGroups map { apiGroup ⇒
      apiGroup.id → Group(
        id = apiGroup.id,
        accessHash = apiGroup.accessHash,
        title = apiGroup.title,
        about = apiGroup.about,
        avatar = apiGroup.avatar.map(buildAvatar),
        isMember = apiGroup.isMember,
        creatorUserId = apiGroup.creatorUserId,
        members = apiGroup.members map (buildMember)
      )
    } toMap
  }

  private def buildMember(apiMember: ApiMember): GroupMember = {
    GroupMember(
      userId = apiMember.userId,
      inviterUserId = apiMember.inviterUserId,
      memberSince = apiMember.date,
      isAdmin = apiMember.isAdmin
    )
  }

  private def buildUsers(apiUsers: Seq[ApiUser]): Map[Int, User] = {
    apiUsers map { apiUser ⇒
      apiUser.id → User(
        id = apiUser.id,
        accessHash = apiUser.accessHash,
        name = apiUser.name,
        sex = apiUser.sex.map(_.id),
        about = apiUser.about,
        avatar = apiUser.avatar.map(buildAvatar),
        username = apiUser.nick,
        isBot = apiUser.isBot
      )
    } toMap
  }

  private def buildAvatar(apiAvatar: ApiAvatar): Avatar = {
    Avatar(
      smallImage = apiAvatar.smallImage.map(buildAvatarImage),
      largeImage = apiAvatar.smallImage.map(buildAvatarImage),
      fullImage = apiAvatar.smallImage.map(buildAvatarImage)
    )
  }

  private def buildAvatarImage(apiAvatarImage: ApiAvatarImage): AvatarImage = {
    AvatarImage(
      fileLocation = buildFileLocation(apiAvatarImage.fileLocation),
      apiAvatarImage.width,
      apiAvatarImage.height,
      fileSize = apiAvatarImage.fileSize
    )
  }

  private def buildFileLocation(apiFileLocation: ApiFileLocation): FileLocation = {
    FileLocation(apiFileLocation.fileId, apiFileLocation.accessHash)
  }
}