package im.actor.server.file

trait UploadKey {
  val key: String
  def toByteArray: Array[Byte]
}