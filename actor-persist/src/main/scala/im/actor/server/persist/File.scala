package im.actor.server.persist

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ SqlAction, FixedSqlAction }

import im.actor.server.models

class FileTable(tag: Tag) extends Table[models.File](tag, "files") {
  def id = column[Long]("id", O.PrimaryKey)

  def accessSalt = column[String]("access_salt")

  def s3UploadKey = column[String]("s3_upload_key")

  def isUploaded = column[Boolean]("is_uploaded")

  def size = column[Long]("size")

  def name = column[String]("name")

  def * = (id, accessSalt, s3UploadKey, isUploaded, size, name) <> (models.File.tupled, models.File.unapply)
}

object File {
  val files = TableQuery[FileTable]

  def create(id: Long, accessSalt: String, s3UploadKey: String): FixedSqlAction[Int, NoStream, Write] =
    files += models.File(id, accessSalt, s3UploadKey, false, 0, "")

  def find(id: Long): SqlAction[Option[models.File], NoStream, Read] =
    files.filter(_.id === id).result.headOption

  def findByKey(key: String) =
    files.filter(_.s3UploadKey === key).result.headOption

  def setUploaded(id: Long, size: Long, name: String) =
    files.filter(_.id === id).map(f ⇒ (f.isUploaded, f.size, f.name)).update((true, size, name))
}
