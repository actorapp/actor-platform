package im.actor.server.models

@SerialVersionUID(1L)
case class FileLocation(fileId: Long, accessHash: Long)

@SerialVersionUID(1L)
case class FileData(id: Long, accessSalt: String, length: Long)

@SerialVersionUID(1L)
case class FileBlock(fileId: Long, offset: Long, length: Long)
