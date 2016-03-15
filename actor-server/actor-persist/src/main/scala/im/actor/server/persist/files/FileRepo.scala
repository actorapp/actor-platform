package im.actor.server.persist.files

import im.actor.server.model.File
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, SqlAction }

final class FileTable(tag: Tag) extends Table[File](tag, "files") {
  def id = column[Long]("id", O.PrimaryKey)
  def accessSalt = column[String]("access_salt")
  def uploadKey = column[String]("upload_key")
  def isUploaded = column[Boolean]("is_uploaded")
  def size = column[Long]("size")
  def name = column[String]("name")

  def * = (id, accessSalt, uploadKey, isUploaded, size, name) <> (File.tupled, File.unapply)
}

object FileRepo {
  val files = TableQuery[FileTable]

  def create(id: Long, expectedSize: Long, accessSalt: String, uploadKey: String): FixedSqlAction[Int, NoStream, Write] =
    files += File(id, accessSalt, uploadKey, isUploaded = false, size = expectedSize, name = "")

  def find(id: Long): SqlAction[Option[File], NoStream, Read] =
    files.filter(_.id === id).result.headOption

  def fetch(ids: Set[Long]) =
    files.filter(_.id inSetBind ids).result

  def findByKey(key: String) =
    files.filter(_.uploadKey === key).result.headOption

  def setUploaded(id: Long, name: String) =
    files.filter(_.id === id).map(f ⇒ (f.isUploaded, f.name)).update((true, name))
}
