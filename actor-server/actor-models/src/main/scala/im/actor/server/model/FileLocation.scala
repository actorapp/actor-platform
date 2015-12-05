package im.actor.server.model

@SerialVersionUID(1L)
case class File(id: Long, accessSalt: String, uploadKey: String, isUploaded: Boolean, size: Long, name: String)

@SerialVersionUID(1L)
case class FilePart(fileId: Long, number: Int, size: Int, uploadKey: String)

@SerialVersionUID(1L)
case class FileBlock(fileId: Long, offset: Long, length: Long)
