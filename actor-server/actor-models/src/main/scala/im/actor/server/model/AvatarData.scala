package im.actor.server.model

object AvatarData {
  trait TypeVal {
    def toInt: Int
  }
  object TypeVal {
    def fromInt(i: Int): TypeVal =
      i match {
        case 1 ⇒ OfUser
        case 2 ⇒ OfGroup
      }
  }
  trait TypeValImpl[T] extends TypeVal
  implicit object OfUser extends TypeValImpl[User] { def toInt = 1; }
  implicit object OfGroup extends TypeValImpl[Group] { def toInt = 1; }
  def typeVal[T]()(implicit impl: TypeValImpl[T]): TypeVal = impl

  def empty[T](entityId: Long)(implicit impl: TypeValImpl[T]): AvatarData =
    empty(impl, entityId)

  def empty(impl: TypeVal, entityId: Long) = AvatarData(
    impl,
    entityId = entityId,
    smallAvatarFileId = None,
    smallAvatarFileHash = None,
    smallAvatarFileSize = None,
    largeAvatarFileId = None,
    largeAvatarFileHash = None,
    largeAvatarFileSize = None,
    fullAvatarFileId = None,
    fullAvatarFileHash = None,
    fullAvatarFileSize = None,
    fullAvatarWidth = None,
    fullAvatarHeight = None
  )
}

case class AvatarData(
  entityType:          AvatarData.TypeVal,
  entityId:            Long,
  smallAvatarFileId:   Option[Long],
  smallAvatarFileHash: Option[Long],
  smallAvatarFileSize: Option[Long],
  largeAvatarFileId:   Option[Long],
  largeAvatarFileHash: Option[Long],
  largeAvatarFileSize: Option[Long],
  fullAvatarFileId:    Option[Long],
  fullAvatarFileHash:  Option[Long],
  fullAvatarFileSize:  Option[Long],
  fullAvatarWidth:     Option[Int],
  fullAvatarHeight:    Option[Int]
) {
  lazy val smallOpt =
    for (
      id ← smallAvatarFileId;
      hash ← smallAvatarFileHash;
      size ← smallAvatarFileSize
    ) yield (id, hash, size)

  lazy val largeOpt =
    for (
      id ← largeAvatarFileId;
      hash ← largeAvatarFileHash;
      size ← largeAvatarFileSize
    ) yield (id, hash, size)

  lazy val fullOpt =
    for (
      id ← fullAvatarFileId;
      hash ← fullAvatarFileHash;
      size ← fullAvatarFileSize;
      w ← fullAvatarWidth;
      h ← fullAvatarHeight
    ) yield (id, hash, size, w, h)
}
