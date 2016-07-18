package im.actor.server.group

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.api.rpc.groups.ApiAdminSettings
import im.actor.api.rpc.misc.ApiExtension
import im.actor.server.cqrs.{ Event, ProcessorState }
import im.actor.server.file.Avatar
import im.actor.server.group.GroupEvents._
import im.actor.server.group.GroupType.{ Channel, General, Public }

private[group] final case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     Instant,
  isAdmin:       Boolean // TODO: remove, use separate admins list instead
)

private[group] final case class Bot(
  userId: Int,
  token:  String
)

object AdminSettings {
  val Default = AdminSettings(
    showAdminsToMembers = true,
    canMembersInvite = true,
    canMembersEditGroupInfo = true,
    canAdminsEditGroupInfo = false
  )

  // format: OFF
  def apiToBitMask(settings: ApiAdminSettings): Int = {
    def toInt(b: Boolean) = if(b) 1 else 0

    List(
      toInt(settings.showAdminsToMembers)     << 0,
      toInt(settings.canMembersInvite)        << 1,
      toInt(settings.canMembersEditGroupInfo) << 2,
      toInt(settings.canAdminsEditGroupInfo)  << 3
    ).sum
  }

  def fromBitMask(mask: Int): AdminSettings = {
    AdminSettings(
      showAdminsToMembers     = (mask & (1 << 0)) != 0,
      canMembersInvite        = (mask & (1 << 1)) != 0,
      canMembersEditGroupInfo = (mask & (1 << 2)) != 0,
      canAdminsEditGroupInfo  = (mask & (1 << 3)) != 0
    )
  }
  // format: ON
}

private[group] final case class AdminSettings(
  showAdminsToMembers:     Boolean, // 1
  canMembersInvite:        Boolean, // 2
  canMembersEditGroupInfo: Boolean, // 4
  canAdminsEditGroupInfo:  Boolean // 8
)

private[group] object GroupState {
  def empty: GroupState =
    GroupState(
      id = 0,
      createdAt = None,
      creatorUserId = 0,
      ownerUserId = 0,
      exUserIds = Set.empty,
      title = "",
      about = None,
      avatar = None,
      topic = None,
      shortName = None,
      typ = GroupType.General,
      isHidden = false,
      isHistoryShared = false,
      members = Map.empty,
      invitedUserIds = Set.empty,
      accessHash = 0L,
      adminSettings = AdminSettings.Default,
      bot = None,

      //???
      extensions = Map.empty
    )
}

private[group] final case class GroupState(
  // creation/ownership
  id:            Int,
  createdAt:     Option[Instant],
  creatorUserId: Int,
  ownerUserId:   Int,
  exUserIds:     Set[Int],

  // group summary info
  title:           String,
  about:           Option[String],
  avatar:          Option[Avatar],
  topic:           Option[String],
  shortName:       Option[String],
  typ:             GroupType, // TODO: rename to groupType
  isHidden:        Boolean,
  isHistoryShared: Boolean,

  // members info
  members:        Map[Int, Member],
  invitedUserIds: Set[Int],

  //security and etc.
  accessHash:    Long,
  adminSettings: AdminSettings,
  bot:           Option[Bot],
  extensions:    Map[Int, Array[Byte]]
) extends ProcessorState[GroupState] {

  lazy val memberIds = members.keySet

  lazy val adminIds = (members filter (_._2.isAdmin == true)).keySet

  lazy val membersCount = members.size

  def isMember(userId: Int): Boolean = members.contains(userId)

  def nonMember(userId: Int): Boolean = !isMember(userId)

  def isInvited(userId: Int): Boolean = invitedUserIds.contains(userId)

  def isBot(userId: Int): Boolean = userId == 0 || (bot exists (_.userId == userId))

  def isAdmin(userId: Int): Boolean = members.get(userId) exists (_.isAdmin)

  // owner will be super-admin in case of channels
  def isOwner(userId: Int): Boolean = userId == ownerUserId

  def isExUser(userId: Int): Boolean = exUserIds.contains(userId)

  val isNotCreated = createdAt.isEmpty

  val isCreated = createdAt.nonEmpty

  def isAsyncMembers =
    typ match {
      case General | Public ⇒ members.size > 100
      case Channel          ⇒ true
    }

  def getShowableOwner(clientUserId: Int): Option[Int] =
    typ match {
      case General | Public ⇒ Some(creatorUserId)
      case Channel          ⇒ if (isAdmin(clientUserId)) Some(creatorUserId) else None
    }

  override def updated(e: Event): GroupState = e match {
    case evt: Created ⇒
      this.copy(
        id = evt.groupId,
        createdAt = Some(evt.ts),
        creatorUserId = evt.creatorUserId,
        ownerUserId = evt.creatorUserId,
        title = evt.title,
        about = None,
        avatar = None,
        topic = None,
        typ = evt.typ.getOrElse(GroupType.General),
        isHidden = evt.isHidden getOrElse false,
        isHistoryShared = evt.isHistoryShared getOrElse false,
        members = (
          evt.userIds map { userId ⇒
            userId →
              Member(
                userId,
                evt.creatorUserId,
                evt.ts,
                isAdmin = userId == evt.creatorUserId
              )
          }
        ).toMap,
        invitedUserIds = evt.userIds.filterNot(_ == evt.creatorUserId).toSet,
        accessHash = evt.accessHash,
        bot = None,
        extensions = (evt.extensions map { //TODO: validate is it right?
          case ApiExtension(extId, data) ⇒
            extId → data
        }).toMap
      )
    case BotAdded(_, userId, token) ⇒
      this.copy(
        bot = Some(Bot(userId, token))
      )
    case UserInvited(ts, userId, inviterUserId) ⇒
      this.copy(
        members = members +
        (userId →
          Member(
            userId,
            inviterUserId,
            invitedAt = ts,
            isAdmin = userId == creatorUserId
          )),
        invitedUserIds = invitedUserIds + userId
      )
    case UserJoined(ts, userId, inviterUserId) ⇒
      this.copy(
        members = members +
        (userId →
          Member(
            userId,
            inviterUserId,
            ts,
            isAdmin = userId == creatorUserId
          )),
        invitedUserIds = invitedUserIds - userId
      )
    case UserKicked(_, userId, _) ⇒
      this.copy(
        members = members - userId,
        invitedUserIds = invitedUserIds - userId,
        exUserIds = exUserIds + userId
      )
    case UserLeft(_, userId) ⇒
      this.copy(
        members = members - userId,
        invitedUserIds = invitedUserIds - userId,
        exUserIds = exUserIds + userId
      )
    case AvatarUpdated(_, newAvatar) ⇒
      this.copy(avatar = newAvatar)
    case TitleUpdated(_, newTitle) ⇒
      this.copy(title = newTitle)
    case BecamePublic(_) ⇒
      this.copy(
        typ = GroupType.Public,
        isHistoryShared = true
      )
    case AboutUpdated(_, newAbout) ⇒
      this.copy(about = newAbout)
    case TopicUpdated(_, newTopic) ⇒
      this.copy(topic = newTopic)
    case AdminStatusChanged(_, userId, isAdmin) ⇒
      this.copy(
        members = members.updated(userId, members(userId).copy(isAdmin = isAdmin))
      )
    case IntegrationTokenRevoked(_, newToken) ⇒
      this.copy(
        bot = bot.map(_.copy(token = newToken))
      )
    case OwnerChanged(_, userId) ⇒
      this.copy(ownerUserId = userId)
    case ShortNameUpdated(_, newShortName) ⇒
      this.copy(shortName = newShortName)
    case AdminSettingsUpdated(_, bitMask) ⇒
      this.copy(adminSettings = AdminSettings.fromBitMask(bitMask))

    // deprecated event
    case UserBecameAdmin(_, userId, _) ⇒
      this.copy(
        members = members.updated(userId, members(userId).copy(isAdmin = true))
      )
  }

  // TODO: real snapshot
  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): GroupState = this

  object permissions {

    /**
     * bot can send messages in all groups
     * in general/public group only members can send messages
     * in channels only owner and admins can send messages
     */
    def canSendMessage(clientUserId: Int) =
      {
        typ match {
          case General | Public ⇒ isMember(clientUserId)
          case Channel          ⇒ isAdmin(clientUserId) || isOwner(clientUserId)
        }
      } || bot.exists(_.userId == clientUserId)

    /**
     * in general/public group, all members can view members
     * in channels, owner and admins can view members
     */
    def canViewMembers(clientUserId: Int) =
      typ match {
        case General | Public ⇒ isMember(clientUserId)
        case Channel          ⇒ isAdmin(clientUserId) || isOwner(clientUserId)
      }

    /**
     * owner and admins always can invite new people
     * members can invite new people if canMembersInvite is true
     */
    def canInvitePeople(clientUserId: Int) =
      isOwner(clientUserId) ||
        isAdmin(clientUserId) ||
        (isMember(clientUserId) && adminSettings.canMembersInvite)

    /**
     * owner always can edit group info
     * admin can edit group info, if canAdminsEditGroupInfo is true in admin settings
     * any member can edit group info, if canMembersEditGroupInfo is true in admin settings
     */
    def canEditInfo(clientUserId: Int): Boolean =
      isOwner(clientUserId) ||
        (isAdmin(clientUserId) && adminSettings.canAdminsEditGroupInfo) ||
        (isMember(clientUserId) && adminSettings.canMembersEditGroupInfo)

    // only owner can change short name
    def canEditShortName(clientUserId: Int): Boolean = isOwner(clientUserId)

    // only owner and other admins can edit admins list
    def canEditAdmins(clientUserId: Int): Boolean =
      isOwner(clientUserId) || isAdmin(clientUserId)

    /**
     * admins list is always visible to owner and admins
     * admins list is visible to any member if showAdminsToMembers = true
     */
    def canViewAdmins(clientUserId: Int): Boolean =
      isOwner(clientUserId) || isAdmin(clientUserId) || adminSettings.showAdminsToMembers

    // only owner can change admin settings
    def canEditAdminSettings(clientUserId: Int): Boolean = isOwner(clientUserId)
  }
}
