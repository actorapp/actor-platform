package im.actor.server.model

@SerialVersionUID(1L)
case class UserPhone(
  id:         Int,
  userId:     Int,
  accessSalt: String,
  number:     Long,
  title:      String
)
