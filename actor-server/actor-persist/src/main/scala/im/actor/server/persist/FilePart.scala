package im.actor.server.persist

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlStreamingAction, FixedSqlAction }

import im.actor.server.models

class FilePartTable(tag: Tag) extends Table[models.FilePart](tag, "file_parts") {
  def fileId = column[Long]("file_id", O.PrimaryKey)

  def number = column[Int]("number", O.PrimaryKey)

  def size = column[Int]("size")

  def s3UploadKey = column[String]("s3_upload_key")

  def * = (fileId, number, size, s3UploadKey) <> (models.FilePart.tupled, models.FilePart.unapply)
}

object FilePart {
  val parts = TableQuery[FilePartTable]

  def createOrUpdate(fileId: Long, number: Int, size: Int, s3UploadKey: String): FixedSqlAction[Int, NoStream, Write] =
    parts.insertOrUpdate(models.FilePart(fileId, number, size, s3UploadKey))

  def findByFileId(fileId: Long): FixedSqlStreamingAction[Seq[models.FilePart], models.FilePart, Read] =
    parts.filter(_.fileId === fileId).sortBy(_.number).result
}