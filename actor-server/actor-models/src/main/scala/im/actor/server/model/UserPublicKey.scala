package im.actor.server.model

@SerialVersionUID(1L)
case class UserPublicKey(
  userId: Int,
  hash:   Long,
  data:   Array[Byte]
)
