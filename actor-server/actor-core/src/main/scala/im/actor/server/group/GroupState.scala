package im.actor.server.group

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.api.rpc.misc.ApiExtension
import im.actor.server.cqrs.{ Event, ProcessorState }
import im.actor.server.file.Avatar
import im.actor.server.group.GroupEvents._
import im.actor.server.group.GroupType.{ Channel, General, Public }

private[group] final case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     Instant,
  isAdmin:       Boolean
)

private[group] final case class Bot(
  userId: Int,
  token:  String
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
      typ = GroupType.General,
      isHidden = false,
      isHistoryShared = false,
      members = Map.empty,
      invitedUserIds = Set.empty,
      accessHash = 0L,
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
  typ:             GroupType, // TODO: rename to groupType
  isHidden:        Boolean,
  isHistoryShared: Boolean,

  // members info
  members:        Map[Int, Member],
  invitedUserIds: Set[Int],

  //security and etc.
  accessHash: Long,
  bot:        Option[Bot],
  extensions: Map[Int, Array[Byte]]
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

  // in case of general/public can view members if user is member
  // in case of channel can view members only if clientUserId is admin
  def canViewMembers(clientUserId: Int) =
    isMember(clientUserId) && ((typ.isGeneral || typ.isPublic) || (typ.isChannel && isAdmin(clientUserId)))

  /**
   * For now, all members can invite other users to group
   */
  def canInvitePeople(clientUserId: Int) = isMember(clientUserId)

  def canSendMessage(clientUserId: Int) =
    {
      typ match {
        case General | Public ⇒ isMember(clientUserId)
        case Channel          ⇒ isAdmin(clientUserId)
      }
    } || bot.exists(_.userId == clientUserId)

  val isNotCreated = createdAt.isEmpty

  val isCreated = createdAt.nonEmpty

  def isAsyncMembers =
    typ match {
      case General | Public ⇒ members.size > 100
      case Channel          ⇒ true
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
    case UserBecameAdmin(_, userId, _) ⇒
      this.copy(
        members = members.updated(userId, members(userId).copy(isAdmin = true))
      )
    case IntegrationTokenRevoked(_, newToken) ⇒
      this.copy(
        bot = bot.map(_.copy(token = newToken))
      )
    case OwnerChanged(_, userId) ⇒
      this.copy(ownerUserId = userId)
  }

  // TODO: real snapshot
  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): GroupState = this
}
