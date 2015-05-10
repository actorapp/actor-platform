package im.actor.server.models

@SerialVersionUID(1L)
case class FileLocation(fileId: Long, accessHash: Long)

@SerialVersionUID(1L)
case class File(id: Long, accessSalt: String, s3UploadKey: String, isUploaded: Boolean, size: Long, name: String)

@SerialVersionUID(1L)
case class FilePart(fileId: Long, number: Int, size: Int, s3UploadKey: String)

@SerialVersionUID(1L)
case class FileBlock(fileId: Long, offset: Long, length: Long)
