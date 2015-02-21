package im.actor.server.models

@SerialVersionUID(1L)
case class Avatar(
  smallImage: Option[AvatarImage],
  largeImage: Option[AvatarImage],
  fullImage: Option[AvatarImage]
) {
  lazy val avatarData = AvatarData(
    smallAvatarFileId = smallImage map (_.fileLocation.fileId),
    smallAvatarFileHash = smallImage map (_.fileLocation.accessHash),
    smallAvatarFileSize = smallImage map (_.fileSize),
    largeAvatarFileId = largeImage map (_.fileLocation.fileId),
    largeAvatarFileHash = largeImage map (_.fileLocation.accessHash),
    largeAvatarFileSize = largeImage map (_.fileSize),
    fullAvatarFileId = fullImage map (_.fileLocation.fileId),
    fullAvatarFileHash = fullImage map (_.fileLocation.accessHash),
    fullAvatarFileSize = fullImage map (_.fileSize),
    fullAvatarWidth = fullImage map (_.width),
    fullAvatarHeight = fullImage map (_.height)
  )
}

object Avatar {
  def empty = Avatar(None, None, None)
}
