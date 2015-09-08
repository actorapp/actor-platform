package im.actor.server

import com.google.protobuf.ByteString
import im.actor.api.rpc.files.{ ApiAvatar, ApiAvatarImage, ApiFileLocation }
import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }

import scala.language.implicitConversions

object ApiConversions {

  implicit def apiToFileLocation(fl: ApiFileLocation): FileLocation =
    FileLocation(fl.fileId, fl.accessHash)

  implicit def apiToAvatarImage(image: ApiAvatarImage): AvatarImage =
    AvatarImage(image.fileLocation, image.width, image.height, image.fileSize.toLong)

  implicit def apiToAvatarImage(imageOpt: Option[ApiAvatarImage]): Option[AvatarImage] =
    imageOpt map apiToAvatarImage

  implicit def apiToAvatar(avatar: ApiAvatar): Avatar =
    Avatar(avatar.smallImage, avatar.largeImage, avatar.fullImage)

  implicit def apiOptToAvatar(avatarOpt: Option[ApiAvatar]): Option[Avatar] =
    avatarOpt map apiToAvatar

  implicit def fileLocationToApi(fl: FileLocation): ApiFileLocation =
    ApiFileLocation(fl.fileId, fl.accessHash)

  implicit def avatarImageToApi(image: AvatarImage): ApiAvatarImage =
    ApiAvatarImage(image.fileLocation, image.width, image.height, image.fileSize.toInt)

  implicit def avatarImageOptToApi(imageOpt: Option[AvatarImage]): Option[ApiAvatarImage] =
    imageOpt map avatarImageToApi

  implicit def avatarToApi(avatar: Avatar): ApiAvatar =
    ApiAvatar(avatar.small, avatar.large, avatar.full)

  implicit def avatarOptToApi(avatarOpt: Option[Avatar]): Option[ApiAvatar] =
    avatarOpt map avatarToApi
}
