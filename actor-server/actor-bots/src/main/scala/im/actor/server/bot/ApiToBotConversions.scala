package im.actor.server.bot

import im.actor.api.rpc.files.{ ApiFileLocation, ApiAvatarImage, ApiAvatar, ApiFastThumb }
import im.actor.api.rpc.groups.{ ApiMember, ApiGroup }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.users.{ ApiContactType, ApiContactRecord, ApiUser }
import scodec.bits.BitVector

import scala.language.{ postfixOps, implicitConversions }

trait ApiToBotConversions {

  import im.actor.bots.BotMessages._

  implicit def toFastThumb(ft: ApiFastThumb): FastThumb =
    FastThumb(ft.w, ft.h, BitVector(ft.thumb).toBase64)

  implicit def toFastThumb(ft: Option[ApiFastThumb]): Option[FastThumb] =
    ft map toFastThumb

  implicit def toDocumentEx(ex: ApiDocumentEx): DocumentEx =
    ex match {
      case ApiDocumentExPhoto(w, h)           ⇒ DocumentExPhoto(w, h)
      case ApiDocumentExVideo(w, h, duration) ⇒ DocumentExVideo(w, h, duration)
      case ApiDocumentExVoice(duration)       ⇒ DocumentExVoice(duration)
    }

  implicit def toDocumentEx(ex: Option[ApiDocumentEx]): Option[DocumentEx] =
    ex map toDocumentEx

  implicit def toMessage(message: ApiMessage): MessageBody =
    message match {
      case ApiTextMessage(text, _, _) ⇒ TextMessage(text)
      case ApiJsonMessage(rawJson)    ⇒ JsonMessage(rawJson)
      case ApiDocumentMessage(
        fileId,
        accessHash,
        fileSize,
        name,
        mimeType,
        thumb,
        ext) ⇒ DocumentMessage(fileId, accessHash, fileSize.toLong, name, mimeType, thumb, ext)
      case ApiServiceMessage(text, _) ⇒ ServiceMessage(text)
      case _: ApiUnsupportedMessage   ⇒ UnsupportedMessage
    }

  implicit def toFileLocation(fl: ApiFileLocation): FileLocation =
    FileLocation(fl.fileId, fl.accessHash)

  implicit def toAvatarImage(ai: ApiAvatarImage): AvatarImage =
    AvatarImage(ai.fileLocation, ai.width, ai.height, ai.fileSize)

  implicit def toAvatarImage(ai: Option[ApiAvatarImage]): Option[AvatarImage] =
    ai map toAvatarImage

  implicit def toAvatar(avatar: ApiAvatar): Avatar =
    Avatar(avatar.smallImage, avatar.largeImage, avatar.fullImage)

  implicit def toAvatar(avatar: Option[ApiAvatar]): Option[Avatar] =
    avatar map toAvatar

  implicit def toMember(apiMember: ApiMember): GroupMember =
    GroupMember(
      userId = apiMember.userId,
      inviterUserId = apiMember.inviterUserId,
      memberSince = apiMember.date,
      isAdmin = apiMember.isAdmin
    )

  implicit def toMembers(apiMembers: Seq[ApiMember]): Seq[GroupMember] =
    apiMembers map toMember

  implicit def toContactRecord(apiContactRecord: ApiContactRecord): ContactRecord =
    apiContactRecord.`type` match {
      case ApiContactType.Email ⇒
        apiContactRecord.stringValue match {
          case Some(email) ⇒ EmailContactRecord(email)
          case None        ⇒ throw new RuntimeException(s"ApiContactRecord with Email type does not contain stringValue: $apiContactRecord")
        }
      case ApiContactType.Phone ⇒
        apiContactRecord.longValue match {
          case Some(phone) ⇒ PhoneContactRecord(phone)
          case None        ⇒ throw new RuntimeException(s"ApiContactRecord with Phone type does not contain longValue: $apiContactRecord")
        }
    }

  implicit def toContactRecords(apiContactRecords: Seq[ApiContactRecord]): Seq[ContactRecord] =
    apiContactRecords map toContactRecord

  implicit def toUser(apiUser: ApiUser): User =
    User(
      id = apiUser.id,
      accessHash = apiUser.accessHash,
      name = apiUser.name,
      sex = apiUser.sex.map(_.id),
      about = apiUser.about,
      avatar = apiUser.avatar,
      username = apiUser.nick,
      isBot = apiUser.isBot,
      contactRecords = apiUser.contactInfo
    )

  implicit def toUsers(apiUsers: Seq[ApiUser]): Seq[User] = apiUsers map toUser

  def buildGroups(apiGroups: Seq[ApiGroup]): Map[Int, Group] = {
    apiGroups map { apiGroup ⇒
      apiGroup.id → Group(
        id = apiGroup.id,
        accessHash = apiGroup.accessHash,
        title = apiGroup.title,
        about = apiGroup.about,
        avatar = apiGroup.avatar,
        isMember = apiGroup.isMember,
        creatorUserId = apiGroup.creatorUserId,
        members = apiGroup.members
      )
    } toMap
  }

  def buildUsers(apiUsers: Seq[ApiUser]): Map[Int, User] = {
    apiUsers map { apiUser ⇒
      apiUser.id → toUser(apiUser)
    } toMap
  }
}