package im.actor.api.rpc

import akka.actor.ActorSystem
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiPeerType, ApiUserOutPeer }
import im.actor.api.rpc.users.ApiUser
import im.actor.server.dialog.HistoryUtils
import im.actor.server.group.GroupExtension
import im.actor.server.user.UserExtension

import scala.concurrent.Future

object EntitiesHelpers {

  private type UsersOrPeers = (Vector[ApiUser], Vector[ApiUserOutPeer])

  private type GroupsOrPeers = (Vector[ApiGroup], Vector[ApiGroupOutPeer])

  /**
   * Load users and groups presented in `dialogs`.
   * If `stripEntities = true`, return user and group peers instead of groups and users
   * If `loadGroupMembers = true` members will be presented in `ApiGroup` object and loaded as users/user peers,
   * otherwise exclude members from users/user peers and `ApiGroup` object.
   */
  def usersAndGroupsByDialogs(
    dialogs:          Seq[ApiDialog],
    stripEntities:    Boolean,
    loadGroupMembers: Boolean
  )(implicit client: AuthorizedClientData, system: ActorSystem): Future[(UsersOrPeers, GroupsOrPeers)] = {
    val (userIds, groupIds) = dialogs.foldLeft((Set.empty[Int], Set.empty[Int])) {
      case ((uacc, gacc), dialog) ⇒
        dialog.peer.`type` match {
          case ApiPeerType.Private | ApiPeerType.EncryptedPrivate ⇒
            (uacc ++ relatedUsers(dialog.message) ++ Set(dialog.peer.id, dialog.senderUserId), gacc)
          case ApiPeerType.Group ⇒
            (uacc ++ relatedUsers(dialog.message) + dialog.senderUserId, gacc + dialog.peer.id)
        }
    }
    usersAndGroupsByIds(groupIds, userIds, stripEntities, loadGroupMembers)
  }

  def usersAndGroupsByShortDialogs(
    dialogs:          Seq[ApiDialogShort],
    stripEntities:    Boolean,
    loadGroupMembers: Boolean
  )(implicit client: AuthorizedClientData, system: ActorSystem): Future[(UsersOrPeers, GroupsOrPeers)] = {
    val (userIds, groupIds) = dialogs.foldLeft((Set.empty[Int], Set.empty[Int])) {
      case ((uids, gids), dialog) ⇒
        dialog.peer.`type` match {
          case ApiPeerType.Group                                  ⇒ (uids, gids + dialog.peer.id)
          case ApiPeerType.Private | ApiPeerType.EncryptedPrivate ⇒ (uids + dialog.peer.id, gids)
        }
    }
    usersAndGroupsByIds(groupIds, userIds, stripEntities, loadGroupMembers)
  }

  def usersAndGroupsByIds(
    groupIds:         Set[Int],
    userIds:          Set[Int],
    stripEntities:    Boolean,
    loadGroupMembers: Boolean
  )(implicit client: AuthorizedClientData, system: ActorSystem): Future[(UsersOrPeers, GroupsOrPeers)] = {
    import system.dispatcher

    for {
      (groupsOrPeers, groupUserIds) ← groupsOrPeers(groupIds, stripEntities, loadGroupMembers)
      usersOrPeers ← usersOrPeers((userIds ++ groupUserIds).toVector, stripEntities)
    } yield (
      usersOrPeers,
      groupsOrPeers
    )
  }

  // get groups or group peers and ids of group members if needed
  private def groupsOrPeers(
    groupIds:         Set[Int],
    stripEntities:    Boolean,
    loadGroupMembers: Boolean
  )(implicit client: AuthorizedClientData, system: ActorSystem): Future[(GroupsOrPeers, Set[Int])] = {
    import system.dispatcher

    for {
      groups ← Future.sequence(groupIds map (GroupExtension(system).getApiStruct(_, client.userId, loadGroupMembers)))
      groupUserIds = if (loadGroupMembers)
        groups.flatMap(g ⇒ g.members.flatMap(m ⇒ Seq(m.userId, m.inviterUserId)) :+ g.creatorUserId)
      else
        Set.empty[Int]
      groupsOrPeers = if (stripEntities) {
        Vector.empty[ApiGroup] → (groups map (g ⇒ ApiGroupOutPeer(g.id, g.accessHash))).toVector
      } else {
        groups.toVector → Vector.empty[ApiGroupOutPeer]
      }
    } yield (
      groupsOrPeers,
      groupUserIds
    )
  }

  // TODO: merge together with method in GroupServiceImpl
  def usersOrPeers(userIds: Vector[Int], stripEntities: Boolean)(implicit client: AuthorizedClientData, system: ActorSystem): Future[UsersOrPeers] = {
    import system.dispatcher
    if (stripEntities) {
      val users = Vector.empty[ApiUser]
      val peers = Future.sequence(userIds filterNot (_ == HistoryUtils.SharedUserId) map { userId ⇒
        UserExtension(system).getAccessHash(userId, client.authId) map (hash ⇒ ApiUserOutPeer(userId, hash))
      })
      peers map (users → _)
    } else {
      val users = Future.sequence(userIds filterNot (_ == HistoryUtils.SharedUserId) map { userId ⇒
        UserExtension(system).getApiStruct(userId, client.userId, client.authId)
      })
      val peers = Vector.empty[ApiUserOutPeer]
      users map (_ → peers)
    }
  }

  def relatedUsers(message: ApiMessage): Set[Int] = {
    message match {
      case ApiServiceMessage(_, extOpt)   ⇒ extOpt map relatedUsers getOrElse Set.empty
      case ApiTextMessage(_, mentions, _) ⇒ mentions.toSet
      case _: ApiJsonMessage              ⇒ Set.empty
      case _: ApiEmptyMessage             ⇒ Set.empty
      case _: ApiDocumentMessage          ⇒ Set.empty
      case _: ApiStickerMessage           ⇒ Set.empty
      case _: ApiUnsupportedMessage       ⇒ Set.empty
      case _: ApiBinaryMessage            ⇒ Set.empty
      case _: ApiEncryptedMessage         ⇒ Set.empty
    }
  }

  private def relatedUsers(ext: ApiServiceEx): Set[Int] =
    ext match {
      case ApiServiceExContactRegistered(userId)                     ⇒ Set(userId)
      case ApiServiceExChangedAvatar(_)                              ⇒ Set.empty
      case ApiServiceExChangedTitle(_)                               ⇒ Set.empty
      case ApiServiceExChangedTopic(_)                               ⇒ Set.empty
      case ApiServiceExChangedAbout(_)                               ⇒ Set.empty
      case ApiServiceExGroupCreated | _: ApiServiceExGroupCreated    ⇒ Set.empty
      case ApiServiceExPhoneCall(_)                                  ⇒ Set.empty
      case ApiServiceExPhoneMissed | _: ApiServiceExPhoneMissed      ⇒ Set.empty
      case ApiServiceExUserInvited(invitedUserId)                    ⇒ Set(invitedUserId)
      case ApiServiceExUserJoined | _: ApiServiceExUserJoined        ⇒ Set.empty
      case ApiServiceExUserKicked(kickedUserId)                      ⇒ Set(kickedUserId)
      case ApiServiceExUserLeft | _: ApiServiceExUserLeft            ⇒ Set.empty
      case _: ApiServiceExChatArchived | _: ApiServiceExChatRestored ⇒ Set.empty
    }

}
