package im.actor.server.bot

import im.actor.bots.BotMessages
import im.actor.server.bots.BotCommand
import im.actor.server.file.{ FileLocation, AvatarImage, Avatar }

import scala.language.implicitConversions

trait BotToInternalConversions {
  implicit def toFileLocation(fl: BotMessages.FileLocation): FileLocation =
    FileLocation(fl.fileId, fl.accessHash)

  implicit def toAvatarImage(image: BotMessages.AvatarImage): AvatarImage =
    AvatarImage(image.fileLocation, image.width, image.height, image.fileSize.toLong)

  implicit def toAvatarImageOpt(imageOpt: Option[BotMessages.AvatarImage]): Option[AvatarImage] =
    imageOpt map toAvatarImage

  implicit def toAvatar(avatar: BotMessages.Avatar): Avatar =
    Avatar(avatar.smallImage, avatar.largeImage, avatar.fullImage)

  implicit def toBotCommand(command: BotMessages.BotCommand): BotCommand =
    BotCommand(command.slashCommand, command.description, command.locKey)
}
