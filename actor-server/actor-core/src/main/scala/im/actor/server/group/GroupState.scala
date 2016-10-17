package im.actor.server.group

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.api.rpc.groups.ApiAdminSettings
import im.actor.api.rpc.misc.ApiExtension
import im.actor.server.cqrs.{ Event, ProcessorState }
import im.actor.server.file.Avatar
import im.actor.server.group.GroupErrors.IncorrectGroupType
import im.actor.server.group.GroupEvents._
import im.actor.server.group.GroupType.{ Channel, General, Unrecognized }

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
  val PlainDefault = AdminSettings(
    showAdminsToMembers = true,
    canMembersInvite = true,
    canMembersEditGroupInfo = true,
    canAdminsEditGroupInfo = true,
    showJoinLeaveMessages = true
  )

  val ChannelsDefault = AdminSettings(
    showAdminsToMembers = false,
    canMembersInvite = false,
    canMembersEditGroupInfo = false,
    canAdminsEditGroupInfo = true,
    showJoinLeaveMessages = false // TODO: figure it out. We don't use it by default
  )

  // format: OFF
  def apiToBitMask(settings: ApiAdminSettings): Int = {
    def toInt(b: Boolean) = if (b) 1 else 0

    (toInt(settings.showAdminsToMembers)     << 0) +
    (toInt(settings.canMembersInvite)        << 1) +
    (toInt(settings.canMembersEditGroupInfo) << 2) +
    (toInt(settings.canAdminsEditGroupInfo)  << 3) +
    (toInt(settings.showJoinLeaveMessages)   << 4)
  }

  def fromBitMask(mask: Int): AdminSettings = {
    AdminSettings(
      showAdminsToMembers     = (mask & (1 << 0)) != 0,
      canMembersInvite        = (mask & (1 << 1)) != 0,
      canMembersEditGroupInfo = (mask & (1 << 2)) != 0,
      canAdminsEditGroupInfo  = (mask & (1 << 3)) != 0,
      showJoinLeaveMessages   = (mask & (1 << 4)) != 0
    )
  }
  // format: ON
}

private[group] final case class AdminSettings(
  showAdminsToMembers:     Boolean, // 1
  canMembersInvite:        Boolean, // 2
  canMembersEditGroupInfo: Boolean, // 4
  canAdminsEditGroupInfo:  Boolean, // 8
  showJoinLeaveMessages:   Boolean // 16
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
      groupType = GroupType.General,
      isHidden = false,
      isHistoryShared = false,
      isAsyncMembers = false,
      members = Map.empty,
      invitedUserIds = Set.empty,
      accessHash = 0L,
      adminSettings = AdminSettings.PlainDefault,
      bot = None,
      deletedAt = None,

      //???
      internalExtensions = Map.empty,
      exts = Seq.empty
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
  groupType:       GroupType,
  isHidden:        Boolean,
  isHistoryShared: Boolean,
  isAsyncMembers:  Boolean,

  // members info
  members:        Map[Int, Member],
  invitedUserIds: Set[Int],

  //security and etc.
  accessHash:         Long,
  adminSettings:      AdminSettings,
  bot:                Option[Bot],
  deletedAt:          Option[Instant],
  internalExtensions: Map[Int, Array[Byte]],
  exts:               Seq[GroupExt]
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

  val isDeleted = deletedAt.nonEmpty

  def getShowableOwner(clientUserId: Int): Option[Int] =
    groupType match {
      case General         ⇒ Some(creatorUserId)
      case Channel         ⇒ if (isAdmin(clientUserId)) Some(creatorUserId) else None
      case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
    }

  override def updated(e: Event): GroupState = e match {
    case evt: Created ⇒
      val typeOfGroup = evt.typ.getOrElse(GroupType.General)
      val isMemberAsync = typeOfGroup.isChannel
      this.copy(
        id = evt.groupId,
        createdAt = Some(evt.ts),
        creatorUserId = evt.creatorUserId,
        ownerUserId = evt.creatorUserId,
        title = evt.title,
        about = None,
        avatar = None,
        topic = None,
        shortName = None,
        groupType = typeOfGroup,
        isHidden = evt.isHidden getOrElse false,
        isHistoryShared = evt.isHistoryShared getOrElse false,
        isAsyncMembers = isMemberAsync,
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
        adminSettings =
          if (typeOfGroup.isChannel) AdminSettings.ChannelsDefault
          else AdminSettings.PlainDefault,
        bot = None,
        internalExtensions = (evt.extensions map { //TODO: validate is it right?
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
    case HistoryBecameShared(_, _) ⇒
      this.copy(isHistoryShared = true)
    case MembersBecameAsync(_) ⇒
      this.copy(isAsyncMembers = true)
    case GroupDeleted(ts, _) ⇒
      // FIXME: don't implement snapshots, before figure out deleted groups behavior
      this.copy(
        deletedAt = Some(ts),

        members = Map.empty,
        invitedUserIds = Set.empty,
        exUserIds = Set.empty,
        bot = None,
        topic = None,
        about = None,
        avatar = None,
        adminSettings =
          if (groupType.isChannel) AdminSettings.ChannelsDefault
          else AdminSettings.PlainDefault
      )
    case ExtAdded(_, ext) ⇒
      if (exts.contains(ext))
        this
      else
        this.copy(exts = exts :+ ext)
    case ExtRemoved(_, key) ⇒
      this.copy(exts = exts.filterNot(_.key == key))
    // deprecated events
    case UserBecameAdmin(_, userId, _) ⇒
      this.copy(
        members = members.updated(userId, members(userId).copy(isAdmin = true))
      )
    case BecamePublic(_) ⇒
      this.copy(isHistoryShared = true)
  }

  // TODO: real snapshot
  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): GroupState = this

  object permissions {

    ///////////////////////////
    //  General permissions  //
    ///////////////////////////

    /**
     * @note check up to date doc in im.actor.api.rpc.groups.ApiGroup
     *
     * Group permissions bits:
     * 0 - canSendMessage. Default is FALSE.
     * 1 - canClear. Default is FALSE.
     * 2 - canLeave. Default is FALSE.
     * 3 - canDelete. Default is FALSE.
     * 4 - canJoin. Default is FALSE.
     * 5 - canViewInfo. Default is FALSE.
     */
    // TODO: add ApiGroupFullPermissions
    def groupFor(userId: Int): Long = {
      ((toInt(canSendMessage(userId)) << 0) +
        (toInt(canClear(userId)) << 1) +
        (toInt(canLeave(userId)) << 2) +
        (toInt(canDelete(userId)) << 3) +
        (toInt(canJoin(userId)) << 4) +
        (toInt(canViewInfo(userId)) << 5)).toLong
    }

    /**
     * bot can send messages in all groups
     * in general/public group only members can send messages
     * in channels only owner and admins can send messages
     */
    private def canSendMessage(clientUserId: Int) =
      {
        groupType match {
          case General         ⇒ isMember(clientUserId)
          case Channel         ⇒ isAdmin(clientUserId) || isOwner(clientUserId)
          case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
        }
      } || bot.exists(_.userId == clientUserId)

    // if  history shared, only owner can clear, everyone otherwise
    private def canClear(clientUserId: Int): Boolean = !isHistoryShared || isOwner(clientUserId)

    /**
     * for now, owner can't leave group.
     * He can either transfer ownership and leave group
     * or delete group completely.
     */
    def canLeave(clientUserId: Int): Boolean = !isOwner(clientUserId)

    // only owner can delete group
    def canDelete(clientUserId: Int): Boolean = isOwner(clientUserId)

    // anyone can join in group with shared history
    def canJoin(clientUserId: Int): Boolean = isHistoryShared

    // if history shared - anyone can view info
    // only members can view info in private groups
    def canViewInfo(clientUserId: Int): Boolean = isHistoryShared || isMember(clientUserId)

    ////////////////////////////
    // Full group permissions //
    ////////////////////////////

    /**
     * @note check up to date doc at im.actor.api.rpc.groups.ApiGroupFull
     *
     * Full group permissions bits:
     * 0 - canEditInfo. Default is FALSE.
     * 1 - canViewMembers. Default is FALSE.
     * 2 - canInviteMembers. Default is FALSE.
     * 3 - canInviteViaLink. Default is FALSE.
     * 4 - canCall. Default is FALSE.
     * 5 - canEditAdminSettings. Default is FALSE.
     * 6 - canViewAdmins. Default is FALSE.
     * 7 - canEditAdmins. Default is FALSE.
     * 8 - canKickInvited. Default is FALSE.
     * 9 - canKickAnyone. Default is FALSE.
     * 10 - canEditForeign. Default is FALSE.
     * 11 - canDeleteForeign. Default is FALSE.
     */
    // TODO: add ApiGroupFullPermissions
    def fullFor(userId: Int): Long = {
      (
        (toInt(canEditInfo(userId)) << 0) +
        (toInt(canViewMembers(userId)) << 1) +
        (toInt(canInviteMembers(userId)) << 2) +
        (toInt(canInviteViaLink(userId)) << 3) +
        (toInt(canCall) << 4) +
        (toInt(canEditAdminSettings(userId)) << 5) +
        (toInt(canViewAdmins(userId)) << 6) +
        (toInt(canEditAdmins(userId)) << 7) +
        (toInt(canKickInvited(userId)) << 8) +
        (toInt(canKickAnyone(userId)) << 9) +
        (toInt(canEditForeign(userId)) << 10) +
        (toInt(canDeleteForeign(userId)) << 11)
      ).toLong
    }

    /**
     * owner always can edit group info
     * admin can edit group info, if canAdminsEditGroupInfo is true in admin settings
     * any member can edit group info, if canMembersEditGroupInfo is true in admin settings
     */
    def canEditInfo(clientUserId: Int): Boolean =
      isOwner(clientUserId) ||
        (isAdmin(clientUserId) && adminSettings.canAdminsEditGroupInfo) ||
        (isMember(clientUserId) && adminSettings.canMembersEditGroupInfo)

    /**
     * in general/public group, all members can view members
     * in channels, owner and admins can view members
     */
    def canViewMembers(clientUserId: Int) =
      groupType match {
        case General         ⇒ isMember(clientUserId)
        case Channel         ⇒ isAdmin(clientUserId) || isOwner(clientUserId)
        case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
      }

    /**
     * owner and admins always can invite new members
     * regular members can invite new members if adminSettings.canMembersInvite is true
     */
    def canInviteMembers(clientUserId: Int) =
      isOwner(clientUserId) ||
        isAdmin(clientUserId) ||
        (isMember(clientUserId) && adminSettings.canMembersInvite)

    /**
     * only owner and admins can invite via link
     */
    private def canInviteViaLink(clientUserId: Int) = isOwner(clientUserId) || isAdmin(clientUserId)

    /**
     * All members can call, if group has less than 25 members, and is not a channel
     */
    def canCall = !groupType.isChannel && membersCount <= 25

    // only owner can change admin settings
    def canEditAdminSettings(clientUserId: Int): Boolean = isOwner(clientUserId)

    /**
     * admins list is always visible to owner and admins
     * admins list is visible to any member if showAdminsToMembers = true
     */
    private def canViewAdmins(clientUserId: Int): Boolean =
      isOwner(clientUserId) || isAdmin(clientUserId) || adminSettings.showAdminsToMembers

    // only owner and other admins can edit admins list
    def canEditAdmins(clientUserId: Int): Boolean =
      isOwner(clientUserId) || isAdmin(clientUserId)

    /**
     * In General group members can kick people they invited
     * In Channel only owner and admins can kick invited people
     */
    def canKickInvited(userId: Int): Boolean =
      groupType match {
        case General         ⇒ isMember(userId)
        case Channel         ⇒ isAdmin(userId) || isOwner(userId)
        case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
      }

    /**
     * Only owner and admins can kick anyone
     */
    def canKickAnyone(userId: Int): Boolean =
      isOwner(userId) || isAdmin(userId)

    /**
     * Only owner and admins can edit foreign messages
     */
    private def canEditForeign(userId: Int): Boolean =
      isOwner(userId) || isAdmin(userId)

    /**
     * Only owner and admins can delete foreign messages
     */
    private def canDeleteForeign(userId: Int): Boolean =
      isOwner(userId) || isAdmin(userId)

    ////////////////////////////
    //  Internal permissions  //
    ////////////////////////////

    // only owner can change short name
    def canEditShortName(clientUserId: Int): Boolean = isOwner(clientUserId)

    // only owner can make history shared
    def canMakeHistoryShared(clientUserId: Int): Boolean = isOwner(clientUserId)

    private def toInt(b: Boolean) = if (b) 1 else 0
  }
}
