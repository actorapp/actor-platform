package im.actor.server.persist.files

import im.actor.server.model.FilePart
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction }

final class FilePartTable(tag: Tag) extends Table[FilePart](tag, "file_parts") {
  def fileId = column[Long]("file_id", O.PrimaryKey)
  def number = column[Int]("number", O.PrimaryKey)
  def size = column[Int]("size")
  def uploadKey = column[String]("upload_key")

  def * = (fileId, number, size, uploadKey) <> (FilePart.tupled, FilePart.unapply)
}

object FilePartRepo {
  val parts = TableQuery[FilePartTable]

  def createOrUpdate(fileId: Long, number: Int, size: Int, uploadKey: String): FixedSqlAction[Int, NoStream, Write] =
    parts.insertOrUpdate(FilePart(fileId, number, size, uploadKey))

  def findByFileId(fileId: Long): FixedSqlStreamingAction[Seq[FilePart], FilePart, Read] =
    parts.filter(_.fileId === fileId).sortBy(_.number).result
}